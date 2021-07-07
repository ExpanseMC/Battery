package me.jellysquid.mods.lithium.mixin.entity.collisions;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import me.jellysquid.mods.lithium.common.entity.movement.BlockCollisionPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Replaces collision testing methods with jumps to our own (faster) entity collision testing code.
 */
@Mixin(CollisionGetter.class)
public interface CollisionGetterMixin {
    /**
     * @reason Use a faster implementation
     * @author JellySquid
     */
    @Overwrite
    default Stream<VoxelShape> getBlockCollisions(final Entity entity, AABB box) {
        return LithiumEntityCollisions.getBlockCollisions((CollisionGetter) this, entity, box, BlockCollisionPredicate.ANY);
    }

    /**
     * @reason Avoid usage of streams
     * @author JellySquid
     */
    @Overwrite
    default boolean noCollision(Entity entity, AABB box, Predicate<Entity> predicate) {
        boolean ret = !LithiumEntityCollisions.doesBoxCollideWithBlocks((CollisionGetter) this, entity, box, BlockCollisionPredicate.ANY);

        // If no blocks were collided with, try to check for entity collisions if we can read entities
        if (ret && this instanceof EntityGetter) {
            ret = !LithiumEntityCollisions.doesBoxCollideWithEntities((EntityGetter) this, entity, box, predicate);
        }

        return ret;
    }
}