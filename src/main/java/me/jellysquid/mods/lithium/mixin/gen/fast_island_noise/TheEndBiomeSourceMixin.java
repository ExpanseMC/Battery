package me.jellysquid.mods.lithium.mixin.gen.fast_island_noise;

import me.jellysquid.mods.lithium.common.world.noise.SimplexNoiseCache;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TheEndBiomeSource.class)
public class TheEndBiomeSourceMixin {
    @Shadow
    @Final
    private SimplexNoise islandNoise;
    private ThreadLocal<SimplexNoiseCache> tlCache;

    @Inject(method = "<init>(Lnet/minecraft/core/Registry;JLnet/minecraft/world/level/biome/Biome;Lnet/minecraft/world/level/biome/Biome;Lnet/minecraft/world/level/biome/Biome;Lnet/minecraft/world/level/biome/Biome;Lnet/minecraft/world/level/biome/Biome;)V",
            at = @At("RETURN"))
    private void battery$hookConstructor(Registry<Biome> registry, long seed, Biome biome, Biome biome2, Biome biome3, Biome biome4, Biome biome5, CallbackInfo ci) {
        this.tlCache = ThreadLocal.withInitial(() -> new SimplexNoiseCache(this.islandNoise));
    }

    /**
     * Use our fast cache instead of vanilla's uncached noise generation.
     */
    @Redirect(method = "getNoiseBiome", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/TheEndBiomeSource;getHeightValue(Lnet/minecraft/world/level/levelgen/synth/SimplexNoise;II)F"))
    private float battery$handleNoiseSample(SimplexNoise simplexNoiseSampler, int x, int z) {
        return this.tlCache.get().getNoiseAt(x, z);
    }
}
