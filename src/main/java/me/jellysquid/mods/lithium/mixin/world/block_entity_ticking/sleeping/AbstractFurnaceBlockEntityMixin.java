package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntitySleepTracker;
import me.jellysquid.mods.lithium.common.world.blockentity.SleepingBlockEntity;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin extends BlockEntity implements SleepingBlockEntity {
    @Shadow
    protected abstract boolean isLit();

    @Shadow
    private int cookingProgress;

    public AbstractFurnaceBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
    }

    private boolean isTicking = true;

    @Override
    public boolean canTickOnSide(boolean isClient) {
        return !isClient;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void battery$checkSleep(CallbackInfo ci) {
        if (!this.isLit() && this.cookingProgress == 0 && this.level != null) {
            this.isTicking = false;
            ((BlockEntitySleepTracker) this.level).setAwake(this, false);
        }
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void battery$wakeUpAfterFromTag(CallbackInfo ci) {
        if (!this.isTicking && this.level != null && !this.level.isClientSide) {
            this.isTicking = true;
            ((BlockEntitySleepTracker) this.level).setAwake(this, true);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!this.isTicking && this.level != null && !this.level.isClientSide) {
            this.isTicking = true;
            ((BlockEntitySleepTracker) this.level).setAwake(this, true);
        }
    }
}
