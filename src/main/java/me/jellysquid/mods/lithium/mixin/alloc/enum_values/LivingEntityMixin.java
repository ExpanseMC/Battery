package me.jellysquid.mods.lithium.mixin.alloc.enum_values;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    private static final EquipmentSlot[] SLOTS = EquipmentSlot.values();

    /**
     * @reason Avoid cloning enum values
     */
    @Redirect(
            method = "collectEquipmentChanges",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EquipmentSlot;values()[Lnet/minecraft/world/entity/EquipmentSlot;"
            )
    )
    private EquipmentSlot[] redirectEquipmentSlotsClone() {
        return SLOTS;
    }
}
