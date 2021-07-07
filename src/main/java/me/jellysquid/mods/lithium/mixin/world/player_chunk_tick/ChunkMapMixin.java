package me.jellysquid.mods.lithium.mixin.world.player_chunk_tick;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.PlayerMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    /**
     * @author JellySquid
     * @reason Defer sending chunks to the player so that we can batch them together
     */
    @Overwrite
    public void move(ServerPlayer player) {
        for (ChunkMap.TrackedEntity tracker : this.entityMap.values()) {
            if (tracker.entity == player) {
                tracker.updatePlayers(this.level.players());
            } else {
                tracker.updatePlayer(player);
            }
        }

        SectionPos oldPos = player.getLastSectionPos();
        SectionPos newPos = SectionPos.of(player);

        boolean isWatchingWorld = this.playerMap.ignored(player);
        boolean doesNotGenerateChunks = this.skipPlayer(player);
        boolean movedSections = !newPos.equals(oldPos);

        if (movedSections || isWatchingWorld != doesNotGenerateChunks) {
            // Notify the client that the chunk map origin has changed. This must happen before any chunk payloads are sent.
            this.updatePlayerPos(player);

            if (!isWatchingWorld) {
                this.distanceManager.removePlayer(oldPos, player);
            }

            if (!doesNotGenerateChunks) {
                this.distanceManager.addPlayer(newPos, player);
            }

            if (!isWatchingWorld && doesNotGenerateChunks) {
                this.playerMap.ignorePlayer(player);
            }

            if (isWatchingWorld && !doesNotGenerateChunks) {
                this.playerMap.unIgnorePlayer(player);
            }

            long oldChunkPos = ChunkPos.asLong(oldPos.getX(), oldPos.getZ());
            long newChunkPos = ChunkPos.asLong(newPos.getX(), newPos.getZ());

            this.playerMap.updatePlayer(oldChunkPos, newChunkPos, player);
        } else {
            // The player hasn't changed locations and isn't changing dimensions
            return;
        }

        // We can only send chunks if the world matches. This hoists a check that
        // would otherwise be performed every time we try to send a chunk over.
        if (player.level == this.level) {
            this.sendChunks(oldPos, player);
        }
    }

    private void sendChunks(SectionPos oldPos, ServerPlayer player) {
        int newCenterX = Mth.floor(player.getX()) >> 4;
        int newCenterZ = Mth.floor(player.getZ()) >> 4;

        int oldCenterX = oldPos.x();
        int oldCenterZ = oldPos.z();

        int watchRadius = this.viewDistance;
        int watchDiameter = watchRadius * 2;

        if (Math.abs(oldCenterX - newCenterX) <= watchDiameter && Math.abs(oldCenterZ - newCenterZ) <= watchDiameter) {
            int minX = Math.min(newCenterX, oldCenterX) - watchRadius;
            int minZ = Math.min(newCenterZ, oldCenterZ) - watchRadius;
            int maxX = Math.max(newCenterX, oldCenterX) + watchRadius;
            int maxZ = Math.max(newCenterZ, oldCenterZ) + watchRadius;

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean isWithinOldRadius = getChunkDistance(x, z, oldCenterX, oldCenterZ) <= watchRadius;
                    boolean isWithinNewRadius = getChunkDistance(x, z, newCenterX, newCenterZ) <= watchRadius;

                    if (isWithinNewRadius && !isWithinOldRadius) {
                        this.startWatchingChunk(player, x, z);
                    }

                    if (isWithinOldRadius && !isWithinNewRadius) {
                        this.stopWatchingChunk(player, x, z);
                    }
                }
            }
        } else {
            for (int x = oldCenterX - watchRadius; x <= oldCenterX + watchRadius; ++x) {
                for (int z = oldCenterZ - watchRadius; z <= oldCenterZ + watchRadius; ++z) {
                    this.stopWatchingChunk(player, x, z);
                }
            }

            for (int x = newCenterX - watchRadius; x <= newCenterX + watchRadius; ++x) {
                for (int z = newCenterZ - watchRadius; z <= newCenterZ + watchRadius; ++z) {
                    this.startWatchingChunk(player, x, z);
                }
            }
        }
    }

    protected void startWatchingChunk(ServerPlayer player, int x, int z) {
        ChunkHolder holder = this.getVisibleChunkIfPresent(ChunkPos.asLong(x, z));

        if (holder != null) {
            LevelChunk chunk = holder.getTickingChunk();

            if (chunk != null) {
                this.playerLoadedChunk(player, new Packet[2], chunk);
            }
        }
    }

    protected void stopWatchingChunk(ServerPlayer player, int x, int z) {
        player.untrackChunk(new ChunkPos(x, z));
    }

    private static int getChunkDistance(int x, int z, int centerX, int centerZ) {
        return Math.max(Math.abs(x - centerX), Math.abs(z - centerZ));
    }

    @Shadow
    @Final
    private Int2ObjectMap<ChunkMap.TrackedEntity> entityMap;

    @Shadow
    @Final
    private ServerLevel level;

    @Shadow
    @Final
    private PlayerMap playerMap;

    @Shadow
    @Final
    private ChunkMap.DistanceManager distanceManager;

    @Shadow
    private int viewDistance;

    @Shadow
    protected abstract boolean skipPlayer(ServerPlayer player);

    @Shadow
    protected abstract SectionPos updatePlayerPos(ServerPlayer serverPlayerEntity);

    @Shadow
    protected abstract ChunkHolder getVisibleChunkIfPresent(long pos);

    @Shadow
    protected abstract void playerLoadedChunk(ServerPlayer player, Packet<?>[] packets, LevelChunk chunk);
}
