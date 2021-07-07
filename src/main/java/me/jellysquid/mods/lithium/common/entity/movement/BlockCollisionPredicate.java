package me.jellysquid.mods.lithium.common.entity.movement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockCollisionPredicate {
    BlockCollisionPredicate ANY = (world, pos, state) -> true;
    BlockCollisionPredicate SUFFOCATES = (world, pos, state) -> state.isSuffocating(world, pos);

    /**
     * @param world The world of which collision tests are being performed in
     * @param pos   The position of the block in the world
     * @param state The block state that is being collided with
     * @return True if the block can be collided with, otherwise false
     */
    boolean test(CollisionGetter world, BlockPos pos, BlockState state);
}
