package me.jellysquid.mods.lithium.common.world.layer;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer0;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2;

public final class CachedLocalLayerFactory {
    public static <R extends Area> AreaFactory<R> createInit(AreaTransformer0 layer, CloneableContext<R> context) {
        return createMemoized(() -> {
            BigContext<R> clonedContext = context.cloneContext();
            return clonedContext.createResult((x, z) -> {
                clonedContext.initRandom(x, z);
                return layer.applyPixel(clonedContext, x, z);
            });
        });
    }

    public static <R extends Area> AreaFactory<R> createParented(AreaTransformer1 layer, CloneableContext<R> context, AreaFactory<R> parent) {
        return createMemoized(() -> {
            BigContext<R> clonedContext = context.cloneContext();
            R parentSampler = parent.make();

            return clonedContext.createResult((x, z) -> {
                clonedContext.initRandom(x, z);
                return layer.applyPixel(clonedContext, parentSampler, x, z);
            }, parentSampler);
        });
    }

    public static <R extends Area> AreaFactory<R> createMerging(AreaTransformer2 layer, CloneableContext<R> context, AreaFactory<R> layer1, AreaFactory<R> layer2) {
        return createMemoized(() -> {
            BigContext<R> clonedContext = context.cloneContext();
            R sampler1 = layer1.make();
            R sampler2 = layer2.make();

            return clonedContext.createResult((x, z) -> {
                clonedContext.initRandom(x, z);
                return layer.applyPixel(clonedContext, sampler1, sampler2, x, z);
            }, sampler1, sampler2);
        });
    }

    private static <R extends Area> AreaFactory<R> createMemoized(AreaFactory<R> factory) {
        ThreadLocal<R> threadLocal = ThreadLocal.withInitial(factory::make);
        return threadLocal::get;
    }
}