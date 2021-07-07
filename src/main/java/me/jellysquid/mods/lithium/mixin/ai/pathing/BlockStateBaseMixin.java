package me.jellysquid.mods.lithium.mixin.ai.pathing;

import me.jellysquid.mods.lithium.api.pathing.BlockPathingBehavior;
import me.jellysquid.mods.lithium.common.ai.pathing.BlockStatePathingCache;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import org.apache.commons.lang3.Validate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin implements BlockStatePathingCache {
    private BlockPathTypes pathNodeType = BlockPathTypes.OPEN;
    private BlockPathTypes pathNodeTypeNeighbor = BlockPathTypes.OPEN;

    @Inject(method = "initCache", at = @At("RETURN"))
    private void battery$init(CallbackInfo ci) {
        BlockState state = this.asState();
        BlockPathingBehavior behavior = (BlockPathingBehavior) this.getBlock();

        this.pathNodeType = Validate.notNull(behavior.getPathNodeType(state));
        this.pathNodeTypeNeighbor = Validate.notNull(behavior.getPathNodeTypeAsNeighbor(state));
    }

    @Override
    public BlockPathTypes getPathNodeType() {
        return this.pathNodeType;
    }

    @Override
    public BlockPathTypes getNeighborPathNodeType() {
        return this.pathNodeTypeNeighbor;
    }

    @Shadow
    protected abstract BlockState asState();

    @Shadow
    public abstract Block getBlock();
}
