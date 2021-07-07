package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.should_tick_cache;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public class LevelMixin {
    @Unique
    private long lastBlockEntityChunkPos = Long.MIN_VALUE;
    @Unique
    private boolean lastShouldTick;

    /**
     * As block entities are loaded in chunk batches, it is likely that the same chunk is queried multiple times in a row.
     * By caching the result we can reduce the amount of chunk manager lookups to one per chunk, assuming no new block
     * entities are created (pistons for example don't meet this criteria).
     */
    @Redirect(
            method = "tickBlockEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/ChunkSource;isTickingChunk(Lnet/minecraft/core/BlockPos;)Z"
            )
    )
    private boolean battery$shouldTick(ChunkSource chunkManager, BlockPos pos) {
        long l = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        if (this.lastBlockEntityChunkPos == l) {
            return this.lastShouldTick;
        } else {
            this.lastBlockEntityChunkPos = l;
            return this.lastShouldTick = chunkManager.isTickingChunk(pos);
        }
    }

    @Inject(
            method = "tickBlockEntities",
            at = @At("RETURN")
    )
    private void battery$clearCache(CallbackInfo ci) {
        this.lastBlockEntityChunkPos = Long.MIN_VALUE;
    }
}
