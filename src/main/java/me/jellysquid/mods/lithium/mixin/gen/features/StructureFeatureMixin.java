package me.jellysquid.mods.lithium.mixin.gen.features;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * Why generate an empty chunk to check for a structure if the chunk's biome cannot generate the
 * structure anyway? Checking the biome first = SPEED!
 *
 * @author TelepathicGrunt
 */
@Mixin(StructureFeature.class)
public class StructureFeatureMixin {

    /**
     * @reason Return null chunk if biome doesn't match structure
     * @author MrGrim
     */
    @Redirect(
            method = "getNearestGeneratedFeature",
            slice = @Slice(
                    from = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, target = "Lnet/minecraft/world/level/chunk/ChunkStatus;STRUCTURE_STARTS:Lnet/minecraft/world/level/chunk/ChunkStatus;", ordinal = 0),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;getPos()Lnet/minecraft/world/level/ChunkPos;", ordinal = 0)
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/LevelReader;getChunk(IILnet/minecraft/world/level/chunk/ChunkStatus;)Lnet/minecraft/world/level/chunk/ChunkAccess;",
                    ordinal = 0
            )
    )
    private ChunkAccess battery$biomeConditionalGetChunk(LevelReader worldView, int x, int z, ChunkStatus status) {
        // Magic numbers << 2) + 2 and biomeY = 0 taken from ChunkGenerator.setStructureStarts
        //noinspection rawtypes
        if (worldView.getNoiseBiome((x << 2) + 2, 0, (z << 2) + 2).getGenerationSettings().isValidStart((StructureFeature) (Object) this)) {
            return worldView.getChunk(x, z, status);
        } else {
            return null;
        }
    }

    /**
     * @reason Can't avoid the call to Chunk.getPos(), and now the chunk might be null.
     * Send a new (0,0) ChunkPos if so. It won't be used anyway.
     * @author MrGrim
     */
    @Redirect(
            method = "getNearestGeneratedFeature",
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getChunk(IILnet/minecraft/world/level/chunk/ChunkStatus;)Lnet/minecraft/world/level/chunk/ChunkAccess;", ordinal = 0),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/core/SectionPos;of(Lnet/minecraft/world/level/ChunkPos;I)Lnet/minecraft/core/SectionPos;", ordinal = 0)
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/ChunkAccess;getPos()Lnet/minecraft/world/level/ChunkPos;", ordinal = 0
            )
    )
    private ChunkPos battery$checkForNull(ChunkAccess chunk) {
        return chunk == null ? new ChunkPos(0, 0) : chunk.getPos();
    }

    /**
     * @reason Return null here if the chunk is null. This will bypass the following if statement
     * allowing the search to continue.
     * @author MrGrim
     */
    @Redirect(
            method = "getNearestGeneratedFeature",
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/core/SectionPos;of(Lnet/minecraft/world/level/ChunkPos;I)Lnet/minecraft/core/SectionPos;", ordinal = 0),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/StructureStart;isValid()Z", ordinal = 0)
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/StructureFeatureManager;getStartForFeature(Lnet/minecraft/core/SectionPos;Lnet/minecraft/world/level/levelgen/feature/StructureFeature;Lnet/minecraft/world/level/chunk/FeatureAccess;)Lnet/minecraft/world/level/levelgen/structure/StructureStart;",
                    ordinal = 0
            )
    )
    private StructureStart<?> checkChunkBeforeGetStructureStart(StructureFeatureManager structureAccessor, SectionPos sectionPos, StructureFeature<?> thisStructure, FeatureAccess chunk) {
        return chunk == null ? null : structureAccessor.getStartForFeature(sectionPos, thisStructure, chunk);
    }
}