package me.jellysquid.mods.lithium.mixin.alloc.enum_values;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {
    private static final Direction[] DIRECTIONS = Direction.values();

    @Redirect(
            method = "getNeighborSignal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/Direction;values()[Lnet/minecraft/core/Direction;"
            )
    )
    private Direction[] redirectShouldExtendDirectionValues() {
        return DIRECTIONS;
    }
}
