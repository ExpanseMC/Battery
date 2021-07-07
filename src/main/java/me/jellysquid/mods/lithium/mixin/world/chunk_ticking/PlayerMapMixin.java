package me.jellysquid.mods.lithium.mixin.world.chunk_ticking;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import me.jellysquid.mods.lithium.common.world.PlayerMapIterable;
import net.minecraft.server.level.PlayerMap;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerMap.class)
public class PlayerMapMixin implements PlayerMapIterable {
    @Shadow
    @Final
    private Object2BooleanMap<ServerPlayer> players;

    @Override
    public Iterable<ServerPlayer> getPlayers() {
        return this.players.keySet();
    }
}
