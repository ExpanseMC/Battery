package me.jellysquid.mods.lithium.mixin.entity.collisions;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import net.minecraft.world.entity.Entity;
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
@Mixin(EntityGetter.class)
public interface EntityGetterMixin {
    /**
     * @reason Avoid usage of heavy stream code
     * @author JellySquid
     */
    @Overwrite
    default Stream<VoxelShape> getEntityCollisions(Entity entity, AABB box, Predicate<Entity> predicate) {
        return LithiumEntityCollisions.getEntityCollisions((EntityGetter) this, entity, box, predicate);
    }
}
