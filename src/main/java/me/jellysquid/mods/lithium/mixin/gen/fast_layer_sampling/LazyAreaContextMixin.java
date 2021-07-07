package me.jellysquid.mods.lithium.mixin.gen.fast_layer_sampling;

import me.jellysquid.mods.lithium.common.world.layer.CachingLayerContextExtended;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.world.level.newbiome.context.LazyAreaContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LazyAreaContext.class)
public class LazyAreaContextMixin implements CachingLayerContextExtended {
    @Shadow
    private long rval;

    @Shadow
    @Final
    private long seed;

    @Override
    public void skipInt() {
        this.rval = LinearCongruentialGenerator.next(this.rval, this.seed);
    }
}