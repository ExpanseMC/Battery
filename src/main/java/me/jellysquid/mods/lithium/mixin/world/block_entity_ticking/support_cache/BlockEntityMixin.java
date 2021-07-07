package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.support_cache;

import me.jellysquid.mods.lithium.common.world.blockentity.SupportCache;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements SupportCache {
    @Shadow
    public abstract BlockState getBlockState();

    @Shadow
    public abstract BlockEntityType<?> getType();

    private BlockState supportTestState;
    private boolean supportTestResult;

    @Override
    public boolean isSupported() {
        BlockState cachedState = this.getBlockState();
        if (this.supportTestState == cachedState) {
            return this.supportTestResult;
        }
        return this.supportTestResult = this.getType().isValid((this.supportTestState = cachedState).getBlock());
    }
}
