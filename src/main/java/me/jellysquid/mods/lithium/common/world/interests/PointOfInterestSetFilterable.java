package me.jellysquid.mods.lithium.common.world.interests;

import me.jellysquid.mods.lithium.common.util.Collector;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import java.util.function.Predicate;

public interface PointOfInterestSetFilterable {
    boolean get(Predicate<PoiType> type, PoiManager.Occupancy status, Collector<PoiRecord> consumer);
}