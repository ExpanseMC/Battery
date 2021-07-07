package me.jellysquid.mods.lithium.mixin.gen.fast_multi_source_biomes;

import com.mojang.datafixers.util.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.data.worldgen.biome.Biomes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

@Mixin(MultiNoiseBiomeSource.class)
public class MultiNoiseBiomeSourceMixin {
    @Shadow
    @Final
    private boolean useY;

    @Shadow
    @Final
    private NormalNoise temperatureNoise;

    @Shadow
    @Final
    private NormalNoise humidityNoise;

    @Shadow
    @Final
    private NormalNoise weirdnessNoise;

    @Shadow
    @Final
    private NormalNoise altitudeNoise;

    @Shadow
    @Final
    private List<Pair<Biome.ClimateParameters, Supplier<Biome>>> parameters;

    /**
     * @reason Remove stream based code in favor of regular collections.
     * @author SuperCoder79
     */
    @Overwrite
    public Biome getNoiseBiome(int biomeX, int biomeY, int biomeZ) {
        // [VanillaCopy] MultiNoiseBiomeSource#getBiomeForNoiseGen

        // Get the y value for perlin noise sampling. This field is always set to false in vanilla code.
        int y = this.useY ? biomeY : 0;

        // Calculate the noise point based using 4 perlin noise samplers.
        Biome.ClimateParameters mixedNoisePoint = new Biome.ClimateParameters(
                (float) this.temperatureNoise.getValue(biomeX, y, biomeZ),
                (float) this.humidityNoise.getValue(biomeX, y, biomeZ),
                (float) this.altitudeNoise.getValue(biomeX, y, biomeZ),
                (float) this.weirdnessNoise.getValue(biomeX, y, biomeZ),
                0.0F
        );

        int idx = -1;
        float min = Float.POSITIVE_INFINITY;

        // Iterate through the biome points and calculate the distance to the current noise point.
        for (int i = 0; i < this.parameters.size(); i++) {
            float distance = this.parameters.get(i).getFirst().fitness(mixedNoisePoint);

            // If the distance is less than the recorded minimum, update the minimum and set the current index.
            if (min > distance) {
                idx = i;
                min = distance;
            }
        }

        // Return the biome with the noise point closest to the evaluated one.
        return this.parameters.get(idx).getSecond().get() == null ? Biomes.THE_VOID : this.parameters.get(idx).getSecond().get();
    }
}
