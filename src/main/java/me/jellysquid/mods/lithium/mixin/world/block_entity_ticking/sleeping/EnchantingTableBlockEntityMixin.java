package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import me.jellysquid.mods.lithium.common.world.blockentity.SleepingBlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnchantmentTableBlockEntity.class)
public class EnchantingTableBlockEntityMixin implements SleepingBlockEntity {
    @Override
    public boolean canTickOnSide(boolean isClient) {
        return isClient;
    }
}
