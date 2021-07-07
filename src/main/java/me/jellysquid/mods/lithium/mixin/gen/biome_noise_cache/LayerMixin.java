package me.jellysquid.mods.lithium.mixin.gen.biome_noise_cache;

import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.area.LazyArea;
import net.minecraft.world.level.newbiome.layer.Layer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Layer.class)
public abstract class LayerMixin {
    private ThreadLocal<LazyArea> tlSampler;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void battery$init(AreaFactory<LazyArea> factory, CallbackInfo ci) {
        this.tlSampler = ThreadLocal.withInitial(factory::make);
    }

    /**
     * @reason Replace with implementation that accesses the thread-local sampler
     * @author gegy1000
     *
     * Original implementation by gegy1000, 2No2Name replaced @Overwrite with @Redirect
     */
    @Redirect(
            method = "get",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/newbiome/area/LazyArea;get(II)I"
            )
    )
    private int battery$sampleThreadLocal(LazyArea cachingLayerSampler, int i, int j) {
        return this.tlSampler.get().get(i, j);
    }
}
