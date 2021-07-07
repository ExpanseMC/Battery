package me.jellysquid.mods.lithium.mixin.entity.inactive_navigations;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.jellysquid.mods.lithium.common.entity.EntityNavigationExtended;
import me.jellysquid.mods.lithium.common.world.ServerWorldExtended;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * This patch is supposed to reduce the cost of setblockstate calls that change the collision shape of a block.
 * In vanilla, changing the collision shape of a block will notify *ALL* EntityNavigations in the world.
 * As EntityNavigations only care about these changes when they actually have a currentPath, we skip the iteration
 * of many navigations. For that optimization we need to keep track of which navigations have a path and which do not.
 *
 * Another possible optimization for the future: If we can somehow find a maximum range that a navigation listens for,
 * we can partition the set by region/chunk/etc. to be able to only iterate over nearby EntityNavigations. In vanilla
 * however, that limit calculation includes the entity position, which can change by a lot very quickly in rare cases.
 * For this optimization we would need to add detection code for very far entity movements. Therefore we don't implement
 * this yet.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements ServerWorldExtended {
    @Mutable
    @Shadow
    @Final
    private Set<PathNavigation> navigations;

    private ReferenceOpenHashSet<PathNavigation> activeEntityNavigations;
    private ArrayList<PathNavigation> activeEntityNavigationUpdates;
    private boolean isIteratingActiveEntityNavigations;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void battery$init(MinecraftServer server, Executor workerExecutor, LevelStorageSource.LevelStorageAccess session, ServerLevelData properties, ResourceKey<Level> registryKey, DimensionType dimensionType, ChunkProgressListener worldGenerationProgressListener, ChunkGenerator chunkGenerator, boolean debugWorld, long l, List<CustomSpawner> list, boolean bl, CallbackInfo ci) {
        this.navigations = new ReferenceOpenHashSet<>(this.navigations);
        this.activeEntityNavigations = new ReferenceOpenHashSet<>();
        this.activeEntityNavigationUpdates = new ArrayList<>();
        this.isIteratingActiveEntityNavigations = false;
    }

    @Redirect(
            method = "add",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;getNavigation()Lnet/minecraft/world/entity/ai/navigation/PathNavigation;"
            )
    )
    private PathNavigation battery$startListeningOnEntityLoad(Mob mobEntity) {
        PathNavigation navigation = mobEntity.getNavigation();
        ((EntityNavigationExtended) navigation).setRegisteredToWorld(true);
        if (navigation.getPath() != null) {
            this.activeEntityNavigations.add(navigation);
        }
        return navigation;
    }

    @Redirect(
            method = "onEntityRemoved",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z"
            )
    )
    private boolean battery$stopListeningOnEntityUnload(Set<PathNavigation> set, Object navigation) {
        PathNavigation entityNavigation = (PathNavigation) navigation;
        ((EntityNavigationExtended) entityNavigation).setRegisteredToWorld(false);
        this.activeEntityNavigations.remove(entityNavigation);
        return set.remove(entityNavigation);
    }

    /**
     * Optimization: Only update listeners that may care about the update. Listeners which have no path
     * never react to the update.
     * With thousands of non-pathfinding mobs in the world, this can be a relevant difference.
     */
    @Redirect(
            method = "sendBlockUpdated",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;iterator()Ljava/util/Iterator;"
            )
    )
    private Iterator<PathNavigation> getActiveListeners(Set<PathNavigation> set) {
        this.isIteratingActiveEntityNavigations = true;
        return this.activeEntityNavigations.iterator();
    }

    @Inject(method = "sendBlockUpdated", at = @At(value = "RETURN"))
    private void battery$onIterationFinished(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
        this.isIteratingActiveEntityNavigations = false;
        if (!this.activeEntityNavigationUpdates.isEmpty()) {
            this.applyActiveEntityNavigationUpdates();
        }
    }

    private void applyActiveEntityNavigationUpdates() {
        ArrayList<PathNavigation> entityNavigationsUpdates = this.activeEntityNavigationUpdates;
        for (int i = entityNavigationsUpdates.size() - 1; i >= 0; i--) {
            PathNavigation entityNavigation = entityNavigationsUpdates.remove(i);
            if (entityNavigation.getPath() != null && ((EntityNavigationExtended) entityNavigation).isRegisteredToWorld()) {
                this.activeEntityNavigations.add(entityNavigation);
            } else {
                this.activeEntityNavigations.remove(entityNavigation);
            }
        }
    }

    @Override
    public void setNavigationActive(Object entityNavigation) {
        PathNavigation entityNavigation1 = (PathNavigation) entityNavigation;
        if (!this.isIteratingActiveEntityNavigations) {
            this.activeEntityNavigations.add(entityNavigation1);
        } else {
            this.activeEntityNavigationUpdates.add(entityNavigation1);
        }
    }

    @Override
    public void setNavigationInactive(Object entityNavigation) {
        PathNavigation entityNavigation1 = (PathNavigation) entityNavigation;
        if (!this.isIteratingActiveEntityNavigations) {
            this.activeEntityNavigations.remove(entityNavigation1);
        } else {
            this.activeEntityNavigationUpdates.add(entityNavigation1);
        }
    }

    protected ServerLevelMixin(WritableLevelData properties, ResourceKey<Level> registryRef, DimensionType dimensionType, Supplier<ProfilerFiller> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);
    }

    /**
     * Debug function
     * @return whether the activeEntityNavigation set is in the correct state
     */
    public boolean isConsistent() {
        int i = 0;
        for (PathNavigation entityNavigation : this.navigations) {
            if ((entityNavigation.getPath() != null && ((EntityNavigationExtended) entityNavigation).isRegisteredToWorld()) != this.activeEntityNavigations.contains(entityNavigation)) {
                return false;
            }
            if (entityNavigation.getPath() != null) {
                i++;
            }
        }
        return this.activeEntityNavigations.size() == i;
    }
}
