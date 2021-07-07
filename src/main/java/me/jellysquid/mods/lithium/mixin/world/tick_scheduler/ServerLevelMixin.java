package me.jellysquid.mods.lithium.mixin.world.tick_scheduler;

import me.jellysquid.mods.lithium.common.world.scheduler.LithiumServerTickScheduler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.TickNextTickData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    /**
     * Redirects the creation of the vanilla server tick scheduler with our own. This only happens once per world load.
     */
    @Redirect(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/world/level/ServerTickList"
            )
    )
    private <T> ServerTickList<T> battery$redirectServerTickSchedulerCtor(ServerLevel world, Predicate<T> invalidPredicate, Function<T, ResourceLocation> idToName, Consumer<TickNextTickData<T>> tickConsumer) {
        return new LithiumServerTickScheduler<>(world, invalidPredicate, idToName, tickConsumer);
    }
}

