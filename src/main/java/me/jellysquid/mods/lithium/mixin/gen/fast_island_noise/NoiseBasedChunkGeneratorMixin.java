package me.jellysquid.mods.lithium.mixin.gen.fast_island_noise;

import me.jellysquid.mods.lithium.common.world.noise.SimplexNoiseCache;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin {
    @Shadow
    @Final
    private SimplexNoise islandNoise;

    private ThreadLocal<SimplexNoiseCache> tlCache;

    @Inject(method = "<init>(Lnet/minecraft/world/level/biome/BiomeSource;Lnet/minecraft/world/level/biome/BiomeSource;JLjava/util/function/Supplier;)V", at = @At("RETURN"))
    private void battery$hookConstructor(BiomeSource biomeSource, BiomeSource biomeSource2, long worldSeed, Supplier<NoiseGeneratorSettings> supplier, CallbackInfo ci) {
        this.tlCache = ThreadLocal.withInitial(() -> new SimplexNoiseCache(this.islandNoise));
    }

    /**
     * Use our fast cache instead of vanilla's uncached noise generation.
     */
    @Redirect(
            method = "fillNoiseColumn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/biome/TheEndBiomeSource;getHeightValue(Lnet/minecraft/world/level/levelgen/synth/SimplexNoise;II)F"
            )
    )
    private float battery$handleNoiseSample(SimplexNoise simplexNoiseSampler, int x, int z) {
        return this.tlCache.get().getNoiseAt(x, z);
    }
}
