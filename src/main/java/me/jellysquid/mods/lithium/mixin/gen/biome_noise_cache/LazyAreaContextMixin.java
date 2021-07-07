package me.jellysquid.mods.lithium.mixin.gen.biome_noise_cache;

import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import me.jellysquid.mods.lithium.common.world.layer.CloneableContext;
import me.jellysquid.mods.lithium.common.world.layer.FastCachingLayerSampler;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.newbiome.area.LazyArea;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.LazyAreaContext;
import net.minecraft.world.level.newbiome.layer.traits.PixelTransformer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LazyAreaContext.class)
public class LazyAreaContextMixin implements CloneableContext<LazyArea> {
    @Shadow
    @Final
    @Mutable
    private long seed;

    @Shadow
    @Final
    @Mutable
    private ImprovedNoise biomeNoise;

    @Shadow
    @Final
    @Mutable
    private Long2IntLinkedOpenHashMap cache;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void battery$init(int cacheCapacity, long seed, long salt, CallbackInfo ci) {
        // We don't use this cache
        this.cache = null;
    }

    /**
     * @reason Replace with optimized cache implementation
     * @author gegy1000
     */
    @Overwrite
    public LazyArea createResult(PixelTransformer operator) {
        return new FastCachingLayerSampler(128, operator);
    }

    /**
     * @reason Replace with optimized cache implementation
     * @author gegy1000
     */
    @Overwrite
    public LazyArea createResult(PixelTransformer operator, LazyArea sampler) {
        return new FastCachingLayerSampler(512, operator);
    }

    /**
     * @reason Replace with optimized cache implementation
     * @author gegy1000
     */
    @Overwrite
    public LazyArea createResult(PixelTransformer operator, LazyArea left, LazyArea right) {
        return new FastCachingLayerSampler(512, operator);
    }

    @Override
    public BigContext<LazyArea> cloneContext() {
        LazyAreaContext context = new LazyAreaContext(0, 0, 0);

        LazyAreaContextMixin access = (LazyAreaContextMixin) (Object) context;
        access.seed = this.seed;
        access.biomeNoise = this.biomeNoise;

        return context;
    }
}
