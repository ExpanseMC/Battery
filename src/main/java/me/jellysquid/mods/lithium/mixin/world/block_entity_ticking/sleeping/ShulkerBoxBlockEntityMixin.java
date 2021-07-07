package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntitySleepTracker;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityMixin extends BlockEntity {

    public ShulkerBoxBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
    }

    @Shadow
    public abstract ShulkerBoxBlockEntity.AnimationStatus getAnimationStatus();

    @Shadow
    private float progressOld;

    @Shadow
    private float progress;

    @Inject(method = "tick", at = @At("RETURN"))
    private void battery$checkSleep(CallbackInfo ci) {
        if (this.getAnimationStatus() == ShulkerBoxBlockEntity.AnimationStatus.CLOSED && this.progressOld == 0f &&
                this.progress == 0f && this.level != null) {
            ((BlockEntitySleepTracker)this.level).setAwake(this, false);
        }
    }

    @Inject(method = "triggerEvent", at = @At("HEAD"))
    public void checkWakeUp(int type, int data, CallbackInfoReturnable<Boolean> cir) {
        if (this.level != null && type == 1) {
            ((BlockEntitySleepTracker)this.level).setAwake(this, true);
        }
    }
}
