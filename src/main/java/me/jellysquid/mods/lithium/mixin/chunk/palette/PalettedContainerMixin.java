package me.jellysquid.mods.lithium.mixin.chunk.palette;

import me.jellysquid.mods.lithium.common.world.chunk.LithiumHashPalette;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.LinearPalette;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PaletteResize;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;

/**
 * Patches {@link PalettedContainer} to make use of {@link LithiumHashPalette}.
 */
@Mixin(value = PalettedContainer.class, priority = 999)
public abstract class PalettedContainerMixin<T> {
    @Shadow
    private Palette<T> palette;

    @Shadow
    protected BitStorage storage;

    @Shadow
    protected abstract void set(int int_1, T object_1);

    @Shadow
    private int bits;

    @Shadow
    @Final
    private Function<CompoundTag, T> reader;

    @Shadow
    @Final
    private Function<T, CompoundTag> writer;

    @Shadow
    @Final
    private IdMapper<T> registry;

    @Shadow
    @Final
    private Palette<T> globalPalette;

    @Shadow
    @Final
    private T defaultValue;

    @Shadow
    protected abstract T get(int int_1);

    /**
     * TODO: Replace this with something that doesn't overwrite.
     *
     * @reason Replace the hash palette from vanilla with our own and change the threshold for usage to only 3 bits,
     * as our implementation performs better at smaller key ranges.
     * @author JellySquid
     */
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Overwrite
    private void setBits(int size) {
        if (size != this.bits) {
            this.bits = size;

            if (this.bits <= 2) {
                this.bits = 2;
                this.palette = new LinearPalette<>(this.registry, this.bits, (PalettedContainer<T>) (Object) this, this.reader);
            } else if (this.bits <= 8) {
                this.palette = new LithiumHashPalette<>(this.registry, this.bits, (PaletteResize<T>) this, this.reader, this.writer);
            } else {
                this.bits = Mth.ceillog2(this.registry.size());
                this.palette = this.globalPalette;
            }

            this.palette.idFor(this.defaultValue);
            this.storage = new BitStorage(this.bits, 4096);
        }
    }

}
