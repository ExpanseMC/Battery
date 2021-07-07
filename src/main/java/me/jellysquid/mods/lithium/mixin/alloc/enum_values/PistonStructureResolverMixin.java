package me.jellysquid.mods.lithium.mixin.alloc.enum_values;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PistonStructureResolver.class)
public class PistonStructureResolverMixin {
    private static final Direction[] VALUES = Direction.values();

    @Redirect(
            method = "addBranchingBlocks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/Direction;values()[Lnet/minecraft/core/Direction;"
            )
    )
    private Direction[] redirectCanMoveAdjacentBlockValues() {
        return VALUES;
    }
}
