package me.jellysquid.mods.lithium.mixin.ai.task;

import me.jellysquid.mods.lithium.common.ai.WeightedListIterable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.WeightedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

public class RunModeMixin {
    @Mixin(targets = "net/minecraft/world/entity/ai/behavior/GateBehavior$RunningPolicy$1")
    public static class RunOneMixin {
        /**
         * @reason Replace stream code with traditional iteration
         * @author JellySquid
         */
        @Overwrite
        public <E extends LivingEntity> void apply(WeightedList<Behavior<? super E>> tasks, ServerLevel world, E entity, long time) {
            for (Behavior<? super E> task : WeightedListIterable.cast(tasks)) {
                if (task.getStatus() == Behavior.Status.STOPPED) {
                    if (task.tryStart(world, entity, time)) {
                        break;
                    }
                }
            }
        }
    }

    @Mixin(targets = "net/minecraft/world/entity/ai/behavior/GateBehavior$RunningPolicy$2")
    public static class TryAllMixin {
        /**
         * @reason Replace stream code with traditional iteration
         * @author JellySquid
         */
        @Overwrite
        public <E extends LivingEntity> void apply(WeightedList<Behavior<? super E>> tasks, ServerLevel world, E entity, long time) {
            for (Behavior<? super E> task : WeightedListIterable.cast(tasks)) {
                if (task.getStatus() == Behavior.Status.STOPPED) {
                    task.tryStart(world, entity, time);
                }
            }
        }
    }
}
