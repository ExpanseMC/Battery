package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.support_cache;

import me.jellysquid.mods.lithium.common.world.blockentity.SupportCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Level.class)
public class LevelMixin {

    @Redirect(
            method = "tickBlockEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
            ),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/util/function/Supplier;)V"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/TickableBlockEntity;tick()V")
            )
    )
    private BlockState battery$getNullBlockState(Level world, BlockPos pos) {
        return null;
    }

    @Redirect(
            method = "tickBlockEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;"
            ),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/util/function/Supplier;)V"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/TickableBlockEntity;tick()V")
            )
    )
    private Block battery$getNullBlock(BlockState blockState) {
        return null;
    }

    @Redirect(
            method = "tickBlockEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/BlockEntity;getType()Lnet/minecraft/world/level/block/entity/BlockEntityType;"
            ),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/util/function/Supplier;)V"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/TickableBlockEntity;tick()V")
            )
    )
    private BlockEntityType<?> getNullIfSupported(BlockEntity blockEntity) {
        return ((SupportCache) blockEntity).isSupported() ? null : BlockEntityType.BANNER;
    }

    @Redirect(
            method = "tickBlockEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/BlockEntityType;isValid(Lnet/minecraft/world/level/block/Block;)Z"
            ),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/util/function/Supplier;)V"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/TickableBlockEntity;tick()V")
            )
    )
    private boolean battery$isFirstArgNull(BlockEntityType<?> blockEntityType, Block block) {
        return blockEntityType == null;
    }

}
