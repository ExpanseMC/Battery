package me.jellysquid.mods.lithium.mixin.world.chunk_inline_block_access;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = LevelChunk.class, priority = 500)
public class LevelChunkMixin {
    private static final BlockState DEFAULT_BLOCK_STATE = Blocks.AIR.defaultBlockState();
    private static final FluidState DEFAULT_FLUID_STATE = Fluids.EMPTY.defaultFluidState();

    @Shadow
    @Final
    private LevelChunkSection[] sections;

    @Shadow
    @Final
    public static LevelChunkSection EMPTY_SECTION;

    /**
     * @reason Reduce method size to help the JVM inline
     * @author JellySquid
     */
    @Overwrite
    public BlockState getBlockState(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        if (!Level.isOutsideBuildHeight(y)) {
            LevelChunkSection section = this.sections[y >> 4];

            if (section != EMPTY_SECTION) {
                return section.getBlockState(x & 15, y & 15, z & 15);
            }
        }

        return DEFAULT_BLOCK_STATE;
    }

    /**
     * @reason Reduce method size to help the JVM inline
     * @author JellySquid, Maity
     */
    @Overwrite
    public FluidState getFluidState(int x, int y, int z) {
        if (!Level.isOutsideBuildHeight(y)) {
            LevelChunkSection section = this.sections[y >> 4];

            if (section != EMPTY_SECTION) {
                return section.getFluidState(x & 15, y & 15, z & 15);
            }
        }

        return DEFAULT_FLUID_STATE;
    }
}
