package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntitySleepTracker;
import me.jellysquid.mods.lithium.common.world.blockentity.SleepingBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkullBlockEntity.class)
public class SkullBlockEntityMixin extends BlockEntity implements SleepingBlockEntity {

    private BlockState lastState;

    public SkullBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
    }

    @Override
    public boolean canTickOnSide(boolean isClient) {
        return isClient;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void battery$checkSleep(CallbackInfo ci) {
        if (this.level != null) {
            BlockState blockState = this.getBlockState();
            if (blockState != this.lastState && !(this.lastState = blockState).is(Blocks.DRAGON_HEAD) && !blockState.is(Blocks.DRAGON_WALL_HEAD)) {
                ((BlockEntitySleepTracker) this.level).setAwake(this, false);
            }
        }
    }

    private void checkWakeUp() {
        if (this.level == null || !this.level.isClientSide()) {
            return;
        }
        BlockState blockState = this.getBlockState();
        if (this.level != null && (blockState.is(Blocks.DRAGON_HEAD) || blockState.is(Blocks.DRAGON_WALL_HEAD))) {
            ((BlockEntitySleepTracker)this.level).setAwake(this, true);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.checkWakeUp();
    }

    @Override
    public void clearCache() {
        super.clearCache();
        this.checkWakeUp();
    }
}
