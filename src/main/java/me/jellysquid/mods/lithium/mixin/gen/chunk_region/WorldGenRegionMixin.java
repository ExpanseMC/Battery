package me.jellysquid.mods.lithium.mixin.gen.chunk_region;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionMixin implements WorldGenLevel {
    @Shadow
    @Final
    private ChunkPos firstPos;

    @Shadow
    @Final
    private int size;

    // Array view of the chunks in the region to avoid an unnecessary de-reference
    private ChunkAccess[] chunksArr;

    // The starting position of this region
    private int minChunkX, minChunkZ;

    /**
     * @author JellySquid
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void battery$init(ServerLevel world, List<ChunkAccess> chunks, CallbackInfo ci) {
        this.minChunkX = this.firstPos.x;
        this.minChunkZ = this.firstPos.z;

        this.chunksArr = chunks.toArray(new ChunkAccess[0]);
    }

    /**
     * @reason Avoid pointer de-referencing, make method easier to inline
     * @author JellySquid
     */
    @Overwrite
    public BlockState getBlockState(BlockPos pos) {
        int x = (pos.getX() >> 4) - this.minChunkX;
        int z = (pos.getZ() >> 4) - this.minChunkZ;
        int w = this.size;

        if (x >= 0 && z >= 0 && x < w && z < w) {
            return this.chunksArr[x + z * w].getBlockState(pos);
        } else {
            throw new NullPointerException("No chunk exists at " + new ChunkPos(pos));
        }
    }

    /**
     * @reason Use the chunk array for faster access
     * @author SuperCoder7979, 2No2Name
     */
    @Overwrite
    public ChunkAccess getChunk(int chunkX, int chunkZ) {
        int x = chunkX - this.minChunkX;
        int z = chunkZ - this.minChunkZ;
        int w = this.size;

        if (x >= 0 && z >= 0 && x < w && z < w) {
            return this.chunksArr[x + z * w];
        } else {
            throw new NullPointerException("No chunk exists at " + new ChunkPos(chunkX, chunkZ));
        }
    }

    /**
     * Use our chunk fetch function
     */
    public ChunkAccess getChunk(BlockPos pos) {
        // Skip checking chunk.getStatus().isAtLeast(ChunkStatus.EMPTY) here, because it is always true
        return this.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }
}
