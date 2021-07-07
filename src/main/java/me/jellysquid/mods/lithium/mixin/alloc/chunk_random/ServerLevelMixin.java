package me.jellysquid.mods.lithium.mixin.alloc.chunk_random;

import me.jellysquid.mods.lithium.common.world.ChunkRandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    private final BlockPos.MutableBlockPos randomPosInChunkCachedPos = new BlockPos.MutableBlockPos();

    /**
     * @reason Avoid allocating BlockPos every invocation through using our allocation-free variant
     */
    @Redirect(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getBlockRandomPos(IIII)Lnet/minecraft/core/BlockPos;"
            )
    )
    private BlockPos battery$redirectTickGetRandomPosInChunk(ServerLevel serverWorld, int x, int y, int z, int mask) {
        ((ChunkRandomSource) serverWorld).getRandomPosInChunk(x, y, z, mask, this.randomPosInChunkCachedPos);

        return this.randomPosInChunkCachedPos;
    }

// TODO(doot): sponge conflict

//    /**
//     * @reason Ensure an immutable block position is passed on block tick
//     */
//    @Redirect(
//            method = "tickChunk",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/world/level/block/state/BlockState;randomTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)V"
//            )
//    )
//    private void battery$redirectBlockStateTick(BlockState blockState, ServerLevel world, BlockPos pos, Random rand) {
//        blockState.randomTick(world, pos.immutable(), rand);
//    }
//
//    /**
//     * @reason Ensure an immutable block position is passed on fluid tick
//     */
//    @Redirect(
//            method = "tickChunk",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/world/level/material/FluidState;randomTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)V"
//            )
//    )
//    private void battery$redirectFluidStateTick(FluidState fluidState, Level world, BlockPos pos, Random rand) {
//        fluidState.randomTick(world, pos.immutable(), rand);
//    }
}
