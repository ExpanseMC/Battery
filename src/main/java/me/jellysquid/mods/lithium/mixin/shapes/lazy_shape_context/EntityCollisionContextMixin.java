package me.jellysquid.mods.lithium.mixin.shapes.lazy_shape_context;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.EntityCollisionContext;

@Mixin(EntityCollisionContext.class)
public class EntityCollisionContextMixin {
    // TODO(doot)
//    @Mutable
//    @Shadow
//    @Final
//    private Item heldItem;
//    @Mutable
//    @Shadow
//    @Final
//    private Predicate<Fluid> canStandOnFluid;
//
//    private Entity lithium_entity;
//
//    /**
//     * Mixin the instanceof to always return false to avoid the expensive inventory access.
//     * No need to use Opcodes.INSTANCEOF or similar.
//     */
//    @ModifyConstant(
//            method = "<init>(Lnet/minecraft/world/entity/Entity;)V",
//            constant = @Constant(classValue = LivingEntity.class, ordinal = 0)
//    )
//    private static boolean battery$redirectInstanceOf(Object obj, Class<?> clazz) {
//        return false;
//    }
//
//    @ModifyConstant(
//            method = "<init>(Lnet/minecraft/world/entity/Entity;)V",
//            constant = @Constant(classValue = LivingEntity.class, ordinal = 2)
//    )
//    private static boolean battery$redirectInstanceOf2(Object obj, Class<?> clazz) {
//        return false;
//    }
//
//    @Inject(
//            method = "<init>(Lnet/minecraft/world/entity/Entity;)V",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/world/phys/shapes/EntityCollisionContext;<init>(ZDLnet/minecraft/world/item/Item;Ljava/util/function/Predicate;)V",
//                    shift = At.Shift.AFTER
//            )
//    )
//    private void battery$initFields(Entity entity, CallbackInfo ci) {
//        this.heldItem = null;
//        this.canStandOnFluid = null;
//        this.lithium_entity = entity;
//    }
//
//    /**
//     * @author 2No2Name
//     * @reason allow skipping unused initialization
//     */
//    @Overwrite
//    public boolean isHoldingItem(Item item) {
//        if (this.heldItem == null) {
//            this.heldItem = this.lithium_entity instanceof LivingEntity ? ((LivingEntity)this.lithium_entity).getMainHandItem().getItem() : Items.AIR;
//        }
//        return this.heldItem == item;
//    }
//
//    /**
//     * @author 2No2Name
//     * @reason allow skipping unused lambda allocation
//     */
//    @Overwrite
//    public boolean canStandOnFluid(FluidState aboveState, FlowingFluid fluid) {
//        return this.lithium_entity instanceof LivingEntity && ((LivingEntity) this.lithium_entity).canStandOnFluid(fluid) && !aboveState.getType().isSame(fluid);
//    }
}
