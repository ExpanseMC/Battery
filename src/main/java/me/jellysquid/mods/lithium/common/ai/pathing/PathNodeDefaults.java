package me.jellysquid.mods.lithium.common.ai.pathing;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public class PathNodeDefaults {
    public static BlockPathTypes getNeighborNodeType(BlockState state) {
        if (state.isAir()) {
            return BlockPathTypes.OPEN;
        }

        // [VanillaCopy] LandPathNodeMaker#getNodeTypeFromNeighbors
        // Determine what kind of obstacle type this neighbor is
        if (state.is(Blocks.CACTUS)) {
            return BlockPathTypes.DANGER_CACTUS;
        } else if (state.is(Blocks.SWEET_BERRY_BUSH)) {
            return BlockPathTypes.DANGER_OTHER;
        } else if (isFireDangerSource(state)) {
            return BlockPathTypes.DANGER_FIRE;
        } else if (state.getFluidState().is(FluidTags.WATER)) {
            return BlockPathTypes.WATER_BORDER;
        } else {
            return BlockPathTypes.OPEN;
        }
    }

    public static BlockPathTypes getNodeType(BlockState state) {
        if (state.isAir()) {
            return BlockPathTypes.OPEN;
        }

        Block block = state.getBlock();
        Material material = state.getMaterial();

        if (state.is(BlockTags.TRAPDOORS) || state.is(Blocks.LILY_PAD)) {
            return BlockPathTypes.TRAPDOOR;
        }

        if (state.is(Blocks.CACTUS)) {
            return BlockPathTypes.DAMAGE_CACTUS;
        }

        if (state.is(Blocks.SWEET_BERRY_BUSH)) {
            return BlockPathTypes.DAMAGE_OTHER;
        }

        if (state.is(Blocks.HONEY_BLOCK)) {
            return BlockPathTypes.STICKY_HONEY;
        }

        if (state.is(Blocks.COCOA)) {
            return BlockPathTypes.COCOA;
        }

        // Retrieve the fluid state from the block state to avoid a second lookup
        FluidState fluidState = state.getFluidState();
        if (fluidState.is(FluidTags.WATER)) {
            return BlockPathTypes.WATER;
        } else if (fluidState.is(FluidTags.LAVA)) {
            return BlockPathTypes.LAVA;
        }

        if (isFireDangerSource(state)) {
            return BlockPathTypes.DAMAGE_FIRE;
        }

        if (DoorBlock.isWoodenDoor(state) && !state.getValue(DoorBlock.OPEN)) {
            return BlockPathTypes.DOOR_WOOD_CLOSED;
        }

        if ((block instanceof DoorBlock) && (material == Material.METAL) && !state.getValue(DoorBlock.OPEN)) {
            return BlockPathTypes.DOOR_IRON_CLOSED;
        }

        if ((block instanceof DoorBlock) && state.getValue(DoorBlock.OPEN)) {
            return BlockPathTypes.DOOR_OPEN;
        }

        if (block instanceof BaseRailBlock) {
            return BlockPathTypes.RAIL;
        }

        if (block instanceof LeavesBlock) {
            return BlockPathTypes.LEAVES;
        }

        if (block.is(BlockTags.FENCES) || block.is(BlockTags.WALLS) || ((block instanceof FenceGateBlock) && !state.getValue(FenceGateBlock.OPEN))) {
            return BlockPathTypes.FENCE;
        }

        return BlockPathTypes.OPEN;
    }

    private static boolean isFireDangerSource(BlockState blockState) {
        return blockState.is(BlockTags.FIRE) || blockState.is(Blocks.LAVA) || blockState.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(blockState);
    }
}
