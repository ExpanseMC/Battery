package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntitySleepTracker;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BellBlockEntity.class)
public class BellBlockEntityMixin extends BlockEntity {

    @Shadow
    private boolean resonating;

    @Shadow
    public boolean shaking;

    public BellBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void battery$checkSleep(CallbackInfo ci) {
        if (!this.shaking && !this.resonating && this.level != null) {
            ((BlockEntitySleepTracker)this.level).setAwake(this, false);
        }
    }

    @Inject(method = "onHit", at = @At("HEAD"))
    public void checkWakeUp(Direction direction, CallbackInfo ci) {
        if (!this.shaking && this.level != null) {
            ((BlockEntitySleepTracker)this.level).setAwake(this, true);
        }
    }

    @Inject(method = "triggerEvent", at = @At("HEAD"))
    public void checkWakeUp(int type, int data, CallbackInfoReturnable<Boolean> cir) {
        if (!this.shaking && type == 1 && this.level != null) {
            ((BlockEntitySleepTracker)this.level).setAwake(this, true);
        }
    }
}
