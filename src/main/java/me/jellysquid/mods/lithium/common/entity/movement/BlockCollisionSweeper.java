package me.jellysquid.mods.lithium.common.entity.movement;

import me.jellysquid.mods.lithium.common.shapes.VoxelShapeCaster;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions.EPSILON;

public class BlockCollisionSweeper {
    private final BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

    /**
     * The collision box being swept through the world.
     */
    private final AABB box;

    /**
     * The VoxelShape of the collision box being swept through the world.
     */
    private final VoxelShape shape;

    private final CollisionGetter view;
    private final CollisionContext context;
    private final Cursor3D cuboidIt;

    private VoxelShape collidedShape;

    public BlockCollisionSweeper(CollisionGetter view, Entity entity, AABB box) {
        this.box = box;
        this.shape = Shapes.create(box);
        this.context = entity == null ? CollisionContext.empty() : CollisionContext.of(entity);
        this.view = view;
        this.cuboidIt = createVolumeIteratorForCollision(box);
    }

    /**
     * Advances the sweep forward by one block, updating the return value of
     * {@link BlockCollisionSweeper#getCollidedShape()} with a block shape if the sweep collided with it.
     *
     * @return True if there are blocks left to be tested, otherwise false
     */
    public boolean step() {
        this.collidedShape = null;

        final Cursor3D cuboidIt = this.cuboidIt;

        if (!cuboidIt.advance()) {
            return false;
        }

        final int edgesHit = cuboidIt.getNextType();

        if (edgesHit == 3) {
            return true;
        }

        final int x = cuboidIt.nextX();
        final int y = cuboidIt.nextY();
        final int z = cuboidIt.nextZ();

        final BlockGetter chunk = this.view.getChunkForCollisions(x >> 4, z >> 4);

        if (chunk == null) {
            return true;
        }

        final BlockPos.MutableBlockPos mpos = this.mpos;
        mpos.set(x, y, z);

        final BlockState state = chunk.getBlockState(mpos);

        if (canInteractWithBlock(state, edgesHit)) {
            VoxelShape collisionShape = state.getCollisionShape(this.view, mpos, this.context);

            if (collisionShape != Shapes.empty()) {
                this.collidedShape = getCollidedShape(this.box, this.shape, collisionShape, x, y, z);
            }
        }

        return true;

    }

    /**
     * @return The shape collided with during the last step, otherwise null
     */
    public VoxelShape getCollidedShape() {
        return this.collidedShape;
    }

    /**
     * Returns an iterator which will include every block position that can contain a collision shape which can interact
     * with the {@param box}.
     */
    private static Cursor3D createVolumeIteratorForCollision(AABB box) {
        int minX = Mth.floor(box.minX - EPSILON) - 1;
        int maxX = Mth.floor(box.maxX + EPSILON) + 1;
        int minY = Mth.floor(box.minY - EPSILON) - 1;
        int maxY = Mth.floor(box.maxY + EPSILON) + 1;
        int minZ = Mth.floor(box.minZ - EPSILON) - 1;
        int maxZ = Mth.floor(box.maxZ + EPSILON) + 1;

        return new Cursor3D(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * This is an artifact from vanilla which is used to avoid testing shapes in the extended portion of a volume
     * unless they are a shape which exceeds their voxel. Pistons must be special-cased here.
     *
     * @return True if the shape can be interacted with at the given edge boundary
     */
    private static boolean canInteractWithBlock(BlockState state, int edgesHit) {
        return (edgesHit != 1 || state.hasLargeCollisionShape()) && (edgesHit != 2 || state.getBlock() == Blocks.MOVING_PISTON);
    }

    /**
     * Checks if the {@param entityShape} or {@param entityBox} intersects the given {@param shape} which is translated
     * to the given position. This is a very specialized implementation which tries to avoid going through VoxelShape
     * for full-cube shapes.
     *
     * @return A {@link VoxelShape} which contains the shape representing that which was collided with, otherwise null
     */
    private static VoxelShape getCollidedShape(AABB entityBox, VoxelShape entityShape, VoxelShape shape, int x, int y, int z) {
        if (shape instanceof VoxelShapeCaster) {
            if (((VoxelShapeCaster) shape).intersects(entityBox, x, y, z)) {
                return shape.move(x, y, z);
            } else {
                return null;
            }
        }

        shape = shape.move(x, y, z);

        if (Shapes.joinIsNotEmpty(shape, entityShape, BooleanOp.AND)) {
            return shape;
        }

        return null;
    }
}
