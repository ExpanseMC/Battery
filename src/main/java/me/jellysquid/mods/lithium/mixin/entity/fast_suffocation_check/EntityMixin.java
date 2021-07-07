package me.jellysquid.mods.lithium.mixin.entity.fast_suffocation_check;

import me.jellysquid.mods.lithium.common.entity.movement.BlockCollisionPredicate;
import me.jellysquid.mods.lithium.common.entity.movement.ChunkAwareBlockCollisionSweeper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.BiPredicate;
import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class EntityMixin {
    /**
     * @author JellySquid
     * @reason Use optimized block volume iteration, avoid streams
     */
    @Redirect(
            method = "isInWall",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getBlockCollisions(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/BiPredicate;)Ljava/util/stream/Stream;"
            )
    )
    public Stream<VoxelShape> isInsideWall(Level world, Entity entity, AABB box, BiPredicate<BlockState, BlockPos> biPredicate) {
        final ChunkAwareBlockCollisionSweeper sweeper = new ChunkAwareBlockCollisionSweeper(world, (Entity) (Object) this, box,
                BlockCollisionPredicate.SUFFOCATES);
        final VoxelShape shape = sweeper.getNextCollidedShape();

        if (shape != null) {
            return Stream.of(shape);
        }

        return Stream.empty();
    }
}
