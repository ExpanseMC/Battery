package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking.goals;

import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityListenerProvider;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityTracker;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LookAtPlayerGoal.class)
public class LookAtPlayerGoalMixin {
    private NearbyEntityTracker<? extends LivingEntity> tracker;

    @Inject(method = "<init>(Lnet/minecraft/world/entity/Mob;Ljava/lang/Class;FF)V", at = @At("RETURN"))
    private void battery$init(Mob mob, Class<? extends LivingEntity> targetType, float range, float chance, CallbackInfo ci) {
        this.tracker = new NearbyEntityTracker<>(targetType, mob, range);

        ((NearbyEntityListenerProvider) mob).getListener().addListener(this.tracker);
    }

    @Redirect(
            method = "canUse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getNearestLoadedEntity(Ljava/lang/Class;Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;Lnet/minecraft/world/entity/LivingEntity;DDDLnet/minecraft/world/phys/AABB;)Lnet/minecraft/world/entity/LivingEntity;"
            )
    )
    private <T extends LivingEntity> LivingEntity redirectGetClosestEntity(Level world, Class<? extends T> entityClass, TargetingConditions targetPredicate, LivingEntity entity, double x, double y, double z, AABB box) {
        return this.tracker.getClosestEntity(box, targetPredicate);
    }

    @Redirect(
            method = "canUse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getNearestPlayer(Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;Lnet/minecraft/world/entity/LivingEntity;DDD)Lnet/minecraft/world/entity/player/Player;"
            )
    )
    private Player battery$redirectGetClosestPlayer(Level world, TargetingConditions targetPredicate, LivingEntity entity, double x, double y, double z) {
        return (Player) this.tracker.getClosestEntity(null, targetPredicate);
    }
}
