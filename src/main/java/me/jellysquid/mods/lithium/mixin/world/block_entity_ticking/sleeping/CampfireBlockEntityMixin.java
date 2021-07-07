package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntitySleepTracker;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin extends BlockEntity {
    @Shadow
    @Final
    private NonNullList<ItemStack> items;

    @Shadow
    @Final
    private int[] cookingProgress;
    @Unique
    private boolean isTicking = true;
    @Unique
    private boolean doInit = true;

    public CampfireBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void battery$firstTick(CallbackInfo ci) {
        if (this.doInit) {
            this.doInit = false;
            this.checkSleepState();
        }
    }
    @Inject(method = "load", at = @At("RETURN"))
    private void battery$wakeUpAfterFromTag(CallbackInfo ci) {
        this.checkSleepState();
    }

    private void checkSleepState() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        boolean shouldTick = false;
        NonNullList<ItemStack> beingCooked = this.items;
        for (int i = 0; i < beingCooked.size(); i++) {
            ItemStack stack = beingCooked.get(i);
            if (!stack.isEmpty()) {
                if (this.cookingProgress[i] > 0 || this.getBlockState().getValue(CampfireBlock.LIT)) {
                    shouldTick = true;
                    break;
                }
            }
        }

        if (shouldTick != this.isTicking) {
            this.isTicking = shouldTick;
            ((BlockEntitySleepTracker)this.level).setAwake(this, shouldTick);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.checkSleepState();
    }

    @Override
    public void clearCache() {
        super.clearCache();
        this.checkSleepState();
    }
}
