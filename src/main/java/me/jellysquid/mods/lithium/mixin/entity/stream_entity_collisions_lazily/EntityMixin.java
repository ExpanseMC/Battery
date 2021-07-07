package me.jellysquid.mods.lithium.mixin.entity.stream_entity_collisions_lazily;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.stream.Stream;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(Entity.class)
public class EntityMixin {
    /**
     * Redirect to try to collide with blocks first, so the entity stream doesn't have to be used when block collisions cancel the whole movement already.
     */
    @Redirect(
            method = "collideBoundingBoxHeuristically",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;concat(Ljava/util/stream/Stream;Ljava/util/stream/Stream;)Ljava/util/stream/Stream;"
            )
    )
    private static Stream<VoxelShape> reorderStreams_BlockCollisionsFirst(Stream<? extends VoxelShape> entityShapes, Stream<? extends VoxelShape> blockShapes) {
        return Stream.concat(blockShapes, entityShapes);
    }
}
