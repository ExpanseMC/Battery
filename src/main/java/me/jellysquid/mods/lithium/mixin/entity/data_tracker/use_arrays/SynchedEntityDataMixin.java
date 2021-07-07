package me.jellysquid.mods.lithium.mixin.entity.data_tracker.use_arrays;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

/**
 * Optimizes the DataTracker to use a simple array-based storage for entries and avoids integer boxing. This reduces
 * a lot of the overhead associated with retrieving tracked data about an entity.
 */
@Mixin(SynchedEntityData.class)
public abstract class SynchedEntityDataMixin {
    private static final int DEFAULT_ENTRY_COUNT = 10, GROW_FACTOR = 8;

    @Mutable
    @Shadow
    @Final
    private Map<Integer, SynchedEntityData.DataItem<?>> itemsById;

    @Shadow
    @Final
    private ReadWriteLock lock;

    /**
     * Mirrors the vanilla backing entries map. Each DataTracker.Entry can be accessed in this array through its ID.
     **/
    private SynchedEntityData.DataItem<?>[] entriesArray = new SynchedEntityData.DataItem<?>[DEFAULT_ENTRY_COUNT];

    /**
     * Re-initialize the backing entries map with an optimized variant to speed up iteration in packet code and to
     * save memory.
     */
    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void battery$reinit(Entity trackedEntity, CallbackInfo ci) {
        this.itemsById = new Int2ObjectOpenHashMap<>(this.itemsById);
    }

    /**
     * We redirect the call to add a tracked data to the internal map so we can add it to our new storage structure. This
     * should only ever occur during entity initialization. Type-erasure is a bit of a pain here since we must redirect
     * a calls to the generic Map interface.
     */
    @Redirect(
            method = "createDataItem",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private Object battery$onAddTrackedDataInsertMap(Map<Class<? extends Entity>, Integer> map, /* Integer */ Object keyRaw, /* DataTracker.Entry<?> */ Object valueRaw) {
        int k = (int) keyRaw;
        SynchedEntityData.DataItem<?> v = (SynchedEntityData.DataItem<?>) valueRaw;

        SynchedEntityData.DataItem<?>[] storage = this.entriesArray;

        // Check if we need to grow the backing array to accommodate the new key range
        if (storage.length <= k) {
            // Grow the array to accommodate 8 entries after this one, but limit it to never be larger
            // than 256 entries as per the vanilla limit
            int newSize = Math.min(k + GROW_FACTOR, 256);

            this.entriesArray = storage = Arrays.copyOf(storage, newSize);
        }

        // Update the storage
        storage[k] = v;

        // Ensure that the vanilla backing storage is still updated appropriately
        return this.itemsById.put(k, v);
    }

    /**
     * @reason Avoid integer boxing/unboxing and use our array-based storage
     * @author JellySquid
     */
    @Overwrite
    private <T> SynchedEntityData.DataItem<T> getItem(EntityDataAccessor<T> data) {
        this.lock.readLock().lock();

        try {
            SynchedEntityData.DataItem<?>[] array = this.entriesArray;

            int id = data.getId();

            // The vanilla implementation will simply return null if the tracker doesn't contain the specified entry. However,
            // accessing an array with an invalid pointer will throw a OOB exception, where-as a HashMap would simply
            // return null. We check this case (which should be free, even if so insignificant, as the subsequent bounds
            // check will hopefully be eliminated)
            if (id < 0 || id >= array.length) {
                return null;
            }

            // This cast can fail if trying to access a entry which doesn't belong to this tracker, as the ID could
            // instead point to an entry of a different type. However, that is also vanilla behaviour.
            // noinspection unchecked
            return (SynchedEntityData.DataItem<T>) array[id];
        } catch (Throwable cause) {
            // Move to another method so this function can be in-lined better
            throw onGetException(cause, data);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    private static <T> ReportedException onGetException(Throwable cause, EntityDataAccessor<T> data) {
        CrashReport report = CrashReport.forThrowable(cause, "Getting synced entity data");

        CrashReportCategory section = report.addCategory("Synced entity data");
        section.setDetail("Data ID", data);

        return new ReportedException(report);
    }
}
