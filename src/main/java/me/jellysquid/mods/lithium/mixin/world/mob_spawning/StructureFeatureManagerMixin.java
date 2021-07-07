package me.jellysquid.mods.lithium.mixin.world.mob_spawning;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StructureFeatureManager.class)
public abstract class StructureFeatureManagerMixin {
    @Shadow
    @Final
    private LevelAccessor level;

    /**
     * @reason Avoid heavily nested stream code and object allocations where possible
     * @author JellySquid
     */
    @Overwrite
    public StructureStart<?> getStructureAt(BlockPos blockPos, boolean fine, StructureFeature<?> feature) {
        ChunkAccess originChunk = this.level.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4, ChunkStatus.STRUCTURE_REFERENCES);

        LongSet references = originChunk.getReferencesForFeature(feature);
        LongIterator iterator = references.iterator();

        while (iterator.hasNext()) {
            long pos = iterator.nextLong();

            ChunkAccess chunk = this.level.getChunk(ChunkPos.getX(pos), ChunkPos.getZ(pos), ChunkStatus.STRUCTURE_STARTS);
            StructureStart<?> structure = chunk.getStartForFeature(feature);

            if (structure == null || !structure.isValid() || !structure.getBoundingBox().isInside(blockPos)) {
                continue;
            }

            if (!fine || this.anyPieceContainsPosition(structure, blockPos)) {
                return structure;
            }
        }

        return StructureStart.INVALID_START;
    }

    private boolean anyPieceContainsPosition(StructureStart<?> structure, BlockPos blockPos) {
        for (StructurePiece piece : structure.getPieces()) {
            if (piece.getBoundingBox().isInside(blockPos)) {
                return true;
            }
        }

        return false;
    }
}
