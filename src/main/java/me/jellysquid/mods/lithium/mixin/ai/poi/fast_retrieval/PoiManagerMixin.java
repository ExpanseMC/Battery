package me.jellysquid.mods.lithium.mixin.ai.poi.fast_retrieval;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import me.jellysquid.mods.lithium.common.util.Collector;
import me.jellysquid.mods.lithium.common.world.interests.PointOfInterestCollectors;
import me.jellysquid.mods.lithium.common.world.interests.RegionBasedStorageSectionAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiSection;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.SectionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(PoiManager.class)
public abstract class PoiManagerMixin extends SectionStorage<PoiSection> {
    public PoiManagerMixin(File directory, Function<Runnable, Codec<PoiSection>> function, Function<Runnable, PoiSection> function2, DataFixer dataFixer, DataFixTypes dataFixTypes, boolean bl) {
        super(directory, function, function2, dataFixer, dataFixTypes, bl);
    }

    /**
     * @reason Retrieve all points of interest in one operation
     * @author JellySquid
     */
    @SuppressWarnings("unchecked")
    @Overwrite
    public Stream<PoiRecord> getInChunk(Predicate<PoiType> predicate, ChunkPos pos, PoiManager.Occupancy status) {
        return ((RegionBasedStorageSectionAccess<PoiSection>) this)
                .getWithinChunkColumn(pos.x, pos.z)
                .flatMap((set) -> set.getRecords(predicate, status));
    }

    /**
     * @reason Retrieve all points of interest in one operation
     * @author JellySquid
     */
    @Overwrite
    public Optional<BlockPos> getRandom(Predicate<PoiType> typePredicate, Predicate<BlockPos> posPredicate, PoiManager.Occupancy status, BlockPos pos, int radius, Random rand) {
        List<PoiRecord> list = this.getAllWithinCircle(typePredicate, pos, radius, status);

        Collections.shuffle(list, rand);

        for (PoiRecord point : list) {
            if (posPredicate.test(point.getPos())) {
                return Optional.of(point.getPos());
            }
        }

        return Optional.empty();
    }

    /**
     * @reason Avoid stream-heavy code, use a faster iterator and callback-based approach
     * @author JellySquid
     */
    @Overwrite
    public Optional<BlockPos> findClosest(Predicate<PoiType> predicate, BlockPos pos, int radius, PoiManager.Occupancy status) {
        List<PoiRecord> points = this.getAllWithinCircle(predicate, pos, radius, status);

        BlockPos nearest = null;
        double nearestDistance = Double.POSITIVE_INFINITY;

        for (PoiRecord point : points) {
            double distance = point.getPos().distSqr(pos);

            if (distance < nearestDistance) {
                nearest = point.getPos();
                nearestDistance = distance;
            }
        }

        return Optional.ofNullable(nearest);
    }

    /**
     * @reason Avoid stream-heavy code, use a faster iterator and callback-based approach
     * @author JellySquid
     */
    @Overwrite
    public long getCountInRange(Predicate<PoiType> predicate, BlockPos pos, int radius, PoiManager.Occupancy status) {
        return this.getAllWithinCircle(predicate, pos, radius, status).size();
    }

    private List<PoiRecord> getAllWithinCircle(Predicate<PoiType> predicate, BlockPos pos, int radius, PoiManager.Occupancy status) {
        List<PoiRecord> points = new ArrayList<>();

        this.collectWithinCircle(predicate, pos, radius, status, points::add);

        return points;
    }

    private void collectWithinCircle(Predicate<PoiType> predicate, BlockPos pos, int radius, PoiManager.Occupancy status, Collector<PoiRecord> collector) {
        Collector<PoiRecord> filter = PointOfInterestCollectors.collectAllWithinRadius(pos, radius, collector);
        Collector<PoiSection> consumer = PointOfInterestCollectors.collectAllMatching(predicate, status, filter);

        int minChunkX = (pos.getX() - radius - 1) >> 4;
        int minChunkZ = (pos.getZ() - radius - 1) >> 4;

        int maxChunkX = (pos.getX() + radius + 1) >> 4;
        int maxChunkZ = (pos.getZ() + radius + 1) >> 4;

        // noinspection unchecked
        RegionBasedStorageSectionAccess<PoiSection> storage = ((RegionBasedStorageSectionAccess<PoiSection>) this);

        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                if (!storage.collectWithinChunkColumn(x, z, consumer)) {
                    return;
                }
            }
        }
    }
}
