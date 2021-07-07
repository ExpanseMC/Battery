package me.jellysquid.mods.lithium.mixin.ai.pathing;

import me.jellysquid.mods.lithium.api.pathing.BlockPathingBehavior;
import me.jellysquid.mods.lithium.common.ai.pathing.PathNodeDefaults;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
public class BlockMixin implements BlockPathingBehavior {
    @Override
    public BlockPathTypes getPathNodeType(BlockState state) {
        return PathNodeDefaults.getNodeType(state);
    }

    @Override
    public BlockPathTypes getPathNodeTypeAsNeighbor(BlockState state) {
        return PathNodeDefaults.getNeighborNodeType(state);
    }
}
