package me.jellysquid.mods.lithium.mixin.alloc.world_ticking;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Shadow
    @Final
    private Int2ObjectMap<Entity> entitiesById;

    @Redirect(
            method = "tick",
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;tick()V"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;add(Lnet/minecraft/world/entity/Entity;)V")
            ),
            at = @At(
                    remap = false,
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/objects/ObjectSet;iterator()Lit/unimi/dsi/fastutil/objects/ObjectIterator;"
            )
    )
    private ObjectIterator<Int2ObjectMap.Entry<Entity>> iterator(ObjectSet<Int2ObjectMap.Entry<Entity>> set) {
        // Avoids allocating a new Map entry object for every iterated value
        return Int2ObjectMaps.fastIterator(this.entitiesById);
    }
}
