package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntitySleepTracker;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderChestBlockEntity.class)
public class EnderChestBlockEntityMixin extends BlockEntity {
    @Shadow
    public int openCount;
    @Shadow
    public float openness;
    @Shadow
    public float oOpenness;
    @Shadow
    private int tickInterval;
    private int lastTime;

    public EnderChestBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void battery$updateTicksOpen(CallbackInfo ci) {
        //noinspection ConstantConditions
        int time = (int) this.level.getGameTime();
        //ticksOpen == 0 implies most likely that this is the first tick. We don't want to update the value then.
        //overflow case is handles by not going to sleep when this.ticksOpen == 0
        if (this.tickInterval != 0) {
            this.tickInterval += time - this.lastTime - 1;
        }
        this.lastTime = time;
    }

    @Inject(method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;blockEvent(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;II)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void battery$checkSleep(CallbackInfo ci) {
        if (this.openCount == 0 && this.openness == 0.0F && this.oOpenness == 0 && this.tickInterval != 0 && this.level != null) {
            ((BlockEntitySleepTracker)this.level).setAwake(this, false);
        }
    }

    @Inject(method = "stopOpen", at = @At("RETURN"))
    private void battery$checkWakeUpOnClose(CallbackInfo ci) {
        this.checkWakeUp();
    }
    @Inject(method = "startOpen", at = @At("RETURN"))
    private void battery$checkWakeUpOnOpen(CallbackInfo ci) {
        this.checkWakeUp();
    }
    @Inject(method = "triggerEvent", at = @At("RETURN"))
    private void battery$checkWakeUpOnSyncedBlockEvent(int type, int data, CallbackInfoReturnable<Boolean> cir) {
        this.checkWakeUp();
    }

    private void checkWakeUp() {
        if ((this.openCount != 0 || this.openness != 0.0F || this.oOpenness != 0) && this.level != null) {
            ((BlockEntitySleepTracker)this.level).setAwake(this, true);
        }
    }
}
