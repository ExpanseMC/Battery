package me.jellysquid.mods.lithium.mixin.gen.fast_layer_sampling;

import me.jellysquid.mods.lithium.common.world.layer.CachingLayerContextExtended;
import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.layer.ZoomLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ZoomLayer.class)
public abstract class ZoomLayerMixin {
    @Shadow
    public abstract int getParentX(int x);

    @Shadow
    public abstract int getParentY(int y);

    @Shadow
    protected abstract int modeOrRandom(BigContext<?> ctx, int tl, int tr, int bl, int br);

    /**
     * @reason Replace with faster implementation.
     * @author gegy1000
     */
    @Overwrite
    public int applyPixel(BigContext<?> ctx, Area parent, int x, int y) {
        // [VanillaCopy] ScaleLayer#sample

        int tl = parent.get(this.getParentX(x), this.getParentY(y));
        int ix = x & 1;
        int iz = y & 1;

        if (ix == 0 && iz == 0) {
            return tl;
        }

        ctx.initRandom(x & ~1, y & ~1);

        if (ix == 0) {
            int bl = parent.get(this.getParentX(x), this.getParentY(y + 1));
            return ctx.random(tl, bl);
        }

        // Move `choose` into above if-statement: maintain rng parity
        ((CachingLayerContextExtended) ctx).skipInt();

        if (iz == 0) {
            int tr = parent.get(this.getParentX(x + 1), this.getParentY(y));
            return ctx.random(tl, tr);
        }

        // Move `choose` into above if-statement: maintain rng parity
        ((CachingLayerContextExtended) ctx).skipInt();

        int bl = parent.get(this.getParentX(x), this.getParentY(y + 1));
        int tr = parent.get(this.getParentX(x + 1), this.getParentY(y));
        int br = parent.get(this.getParentX(x + 1), this.getParentY(y + 1));

        return this.modeOrRandom(ctx, tl, tr, bl, br);
    }
}