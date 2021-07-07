package me.jellysquid.mods.lithium.mixin.ai.task;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

@Mixin(Behavior.class)
public class BehaviorMixin<E extends LivingEntity> {
    @Mutable
    @Shadow
    @Final
    protected Map<MemoryModuleType<?>, MemoryStatus> entryCondition;

    @Inject(method = "<init>(Ljava/util/Map;II)V", at = @At("RETURN"))
    private void battery$init(Map<MemoryModuleType<?>, MemoryStatus> map, int int_1, int int_2, CallbackInfo ci) {
        this.entryCondition = new Reference2ObjectOpenHashMap<>(map);
    }

    /**
     * @reason Replace stream-based code with traditional iteration, use a flattened array list to avoid pointer chasing
     * @author JellySquid
     */
    @Overwrite
    private boolean hasRequiredMemories(E entity) {
        Iterable<Reference2ObjectMap.Entry<MemoryModuleType<?>, MemoryStatus>> iterable =
                Reference2ObjectMaps.fastIterable((Reference2ObjectOpenHashMap<MemoryModuleType<?>, MemoryStatus>) this.entryCondition);

        for (Reference2ObjectMap.Entry<MemoryModuleType<?>, MemoryStatus> entry : iterable) {
            if (!entity.getBrain().checkMemory(entry.getKey(), entry.getValue())) {
                return false;
            }
        }

        return true;
    }
}
