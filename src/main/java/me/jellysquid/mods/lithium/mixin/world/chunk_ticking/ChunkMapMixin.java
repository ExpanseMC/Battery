package me.jellysquid.mods.lithium.mixin.world.chunk_ticking;

import me.jellysquid.mods.lithium.common.world.PlayerMapIterable;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.PlayerMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    @Shadow
    @Final
    private ChunkMap.DistanceManager distanceManager;

    @Shadow
    @Final
    private PlayerMap playerMap;

    @Shadow
    private static double euclideanDistanceSquared(ChunkPos pos, Entity entity) {
        throw new UnsupportedOperationException();
    }

    /**
     * The usage of stream code here can be rather costly, as this method will be called for every loaded chunk each
     * tick in order to determine if a player is close enough to allow for mob spawning. This implementation avoids
     * object allocations and uses a traditional iterator based approach, providing a significant boost to how quickly
     * the game can tick chunks.
     *
     * @reason Use optimized implementation
     * @author JellySquid
     */
    @Overwrite
    @SuppressWarnings("ConstantConditions")
    public boolean noPlayersCloseForSpawning(ChunkPos pos) {
        long key = pos.toLong();

        if (!this.distanceManager.hasPlayersNearby(key)) {
            return true;
        }

        for (ServerPlayer player : ((PlayerMapIterable) (Object) this.playerMap).getPlayers()) {
            // [VanillaCopy] Only non-spectator players within 128 blocks of the chunk can enable mob spawning
            if (!player.isSpectator() && euclideanDistanceSquared(pos, player) < 16384.0D) {
                return false;
            }
        }

        // No matching players were nearby, so mobs cannot currently be spawned here
        return true;
    }
}
