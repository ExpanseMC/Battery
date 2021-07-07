package me.jellysquid.mods.lithium.mixin.ai.task;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

@Mixin(SetLookAndInteract.class)
public abstract class SetLookAndInteractMixin extends Behavior<LivingEntity> {
    @Shadow
    @Final
    private Predicate<LivingEntity> selfFilter;

    @Shadow
    protected abstract List<LivingEntity> getVisibleEntities(LivingEntity entity);

    @Shadow
    protected abstract boolean isMatchingTarget(LivingEntity entity);

    @Shadow
    @Final
    private int interactionRangeSqr;

    public SetLookAndInteractMixin(Map<MemoryModuleType<?>, MemoryStatus> memories) {
        super(memories);
    }

    /**
     * @reason Replace stream code with traditional iteration
     * @author JellySquid
     */
    @Overwrite
    public boolean checkExtraStartConditions(ServerLevel world, LivingEntity entity) {
        if (!this.selfFilter.test(entity)) {
            return false;
        }

        List<LivingEntity> visibleEntities = this.getVisibleEntities(entity);

        for (LivingEntity otherEntity : visibleEntities) {
            if (this.isMatchingTarget(otherEntity)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @reason Replace stream code with traditional iteration
     * @author JellySquid
     */
    @Overwrite
    public void start(ServerLevel world, LivingEntity entity, long time) {
        super.start(world, entity, time);

        Brain<?> brain = entity.getBrain();

        List<LivingEntity> visibleEntities = brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES)
                .orElse(Collections.emptyList());

        for (LivingEntity otherEntity : visibleEntities) {
            if (otherEntity.distanceToSqr(entity) > (double) this.interactionRangeSqr) {
                continue;
            }

            if (this.isMatchingTarget(otherEntity)) {
                brain.setMemory(MemoryModuleType.INTERACTION_TARGET, otherEntity);
                brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(otherEntity, true));

                break;
            }
        }
    }

}
