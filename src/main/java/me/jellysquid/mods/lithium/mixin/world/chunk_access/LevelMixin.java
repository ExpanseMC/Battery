package me.jellysquid.mods.lithium.mixin.world.chunk_access;

import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Implement the interface members of {@link LevelReader} and {@link CollisionGetter} directly to avoid complicated
 * method invocations between interface boundaries, helping the JVM to inline and optimize code.
 */
@Mixin(Level.class)
public abstract class LevelMixin implements LevelAccessor {
    /**
     * @reason Remove dynamic-dispatch and inline call
     * @author JellySquid
     */
    @Overwrite
    public LevelChunk getChunkAt(BlockPos pos) {
        return (LevelChunk) this.getChunk(pos);
    }

    @Override
    public ChunkAccess getChunk(BlockPos pos) {
        return this.getChunkLithium(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, true);
    }

    /**
     * @reason Remove dynamic-dispatch and inline call
     * @author JellySquid
     */
    @Override
    @Overwrite
    public LevelChunk getChunk(int chunkX, int chunkZ) {
        return (LevelChunk) this.getChunkLithium(chunkX, chunkZ, ChunkStatus.FULL, true);
    }

    @Override
    public ChunkAccess getChunk(int chunkX, int chunkZ, ChunkStatus status) {
        return this.getChunkLithium(chunkX, chunkZ, status, true);
    }

    @Override
    public BlockGetter getChunkForCollisions(int chunkX, int chunkZ) {
        return this.getChunkLithium(chunkX, chunkZ, ChunkStatus.FULL, false);
    }

    private ChunkAccess getChunkLithium(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
        ChunkAccess chunk = this.getChunkSource().getChunk(chunkX, chunkZ, leastStatus, create);

        if (chunk == null && create) {
            throw new IllegalStateException("Should always be able to create a chunk!");
        } else {
            return chunk;
        }
    }
}
