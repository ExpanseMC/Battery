package me.jellysquid.mods.lithium.mixin.entity.gravity_check_block_below;

import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.stream.Stream;

@Mixin(Shapes.class)
public class ShapesMixin {
    /**
     * Check the block below the entity first, as it is the block that is most likely going to cancel the movement from
     * gravity.
     */
    @Inject(
            method = "collide(Lnet/minecraft/world/phys/AABB;Lnet/minecraft/world/level/LevelReader;DLnet/minecraft/world/phys/shapes/CollisionContext;Lnet/minecraft/core/AxisCycle;Ljava/util/stream/Stream;)D",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/AxisCycle;inverse()Lnet/minecraft/core/AxisCycle;",
                    ordinal = 0
            ),
            cancellable = true,
            locals = LocalCapture.NO_CAPTURE
    )
    private static void checkBelowFeet(AABB box, LevelReader world, double movement, CollisionContext context, AxisCycle direction, Stream<VoxelShape> shapes, CallbackInfoReturnable<Double> cir) {
        // [VanillaCopy] calculate axis of movement like vanilla: direction.opposite().cycle(...)
        // Necessary due to the method not simply explicitly receiving the axis of the movement
        if (movement >= 0 || direction.inverse().cycle(Direction.Axis.Z) != Direction.Axis.Y) {
            return;
        }

        // Here the movement axis must be Axis.Y, and the movement is negative / downwards
        int x = Mth.floor((box.minX + box.maxX) / 2);
        int y = Mth.ceil(box.minY) - 1;
        int z = Mth.floor((box.minZ + box.maxZ) / 2);
        BlockPos pos = new BlockPos(x, y, z);

        // [VanillaCopy] collide with the block below the center of the box exactly like vanilla does during block iteration
        BlockState blockState = world.getBlockState(pos);
        movement = blockState.getCollisionShape(world, pos, context).collide(Direction.Axis.Y, box.move(-x, -y, -z), movement);
        if (Math.abs(movement) < 1.0E-7D) {
            cir.setReturnValue(0.0D);
        }
    }
}
