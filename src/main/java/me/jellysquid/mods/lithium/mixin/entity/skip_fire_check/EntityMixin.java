package me.jellysquid.mods.lithium.mixin.entity.skip_fire_check;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.stream.Stream;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    private int remainingFireTicks;

    @Shadow
    protected abstract int getFireImmuneTicks();

    @Redirect(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getBlockStatesIfLoaded(Lnet/minecraft/world/phys/AABB;)Ljava/util/stream/Stream;"
            )
    )
    private Stream<BlockState> skipFireTestIfResultDoesNotMatter(Level world, AABB box) {
        // Skip scanning the blocks around the entity touches by returning an empty stream when the result does not matter
        if (this.remainingFireTicks > 0 || this.remainingFireTicks == -this.getFireImmuneTicks()) {
            return Stream.empty();
        }

        return world.getBlockStatesIfLoaded(box);
    }
}
