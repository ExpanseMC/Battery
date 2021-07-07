package me.jellysquid.mods.lithium.mixin.world.chunk_task_system;

import me.jellysquid.mods.lithium.common.util.thread.ArrayPrioritizedTaskQueue;
import net.minecraft.server.level.ChunkTaskPriorityQueueSorter;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.util.thread.StrictQueue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * This replaces the queue used by the chunk job executor to a much quicker variant.
 */
@Mixin(ChunkTaskPriorityQueueSorter.class)
public class ChunkTaskPriorityQueueSorterMixin {
    @Mutable
    @Shadow
    @Final
    private ProcessorMailbox<StrictQueue.IntRunnable> mailbox;

    /**
     * Re-initialize the task executor with our optimized task queue type. This is a safe operation that happens only
     * once at world load. No tasks will be enqueued until after the constructor is ran, so we do not need to worry
     * about copying them.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void battery$init(List<ProcessorHandle<?>> listeners, Executor executor, int maxQueues, CallbackInfo ci) {
        this.mailbox = new ProcessorMailbox<>(new ArrayPrioritizedTaskQueue(4), executor, "sorter");
    }
}
