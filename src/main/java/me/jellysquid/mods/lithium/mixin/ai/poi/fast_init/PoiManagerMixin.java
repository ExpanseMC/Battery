package me.jellysquid.mods.lithium.mixin.ai.poi.fast_init;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import me.jellysquid.mods.lithium.common.world.interests.PointOfInterestTypeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiSection;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.storage.SectionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Mixin(PoiManager.class)
public abstract class PoiManagerMixin extends SectionStorage<PoiSection> {
    public PoiManagerMixin(File directory, Function<Runnable, Codec<PoiSection>> function, Function<Runnable, PoiSection> function2, DataFixer dataFixer, DataFixTypes dataFixTypes, boolean bl) {
        super(directory, function, function2, dataFixer, dataFixTypes, bl);
    }

    @Shadow
    protected abstract void updateFromSection(LevelChunkSection section, SectionPos sectionPos, BiConsumer<BlockPos, PoiType> entryConsumer);

    /**
     * @reason Avoid Stream API
     * @author Jellysquid
     */
    @Overwrite
    public void checkConsistencyWithBlocks(ChunkPos chunkPos_1, LevelChunkSection section) {
        SectionPos sectionPos = SectionPos.of(chunkPos_1, section.bottomBlockY() >> 4);

        PoiSection set = this.getOrLoad(sectionPos.asLong()).orElse(null);

        if (set != null) {
            set.refresh((consumer) -> {
                if (PointOfInterestTypeHelper.shouldScan(section)) {
                    this.updateFromSection(section, sectionPos, consumer);
                }
            });
        } else {
            if (PointOfInterestTypeHelper.shouldScan(section)) {
                set = this.getOrCreate(sectionPos.asLong());

                this.updateFromSection(section, sectionPos, set::add);
            }
        }
    }
}
