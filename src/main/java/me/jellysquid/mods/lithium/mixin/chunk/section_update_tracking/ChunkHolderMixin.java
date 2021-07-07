package me.jellysquid.mods.lithium.mixin.chunk.section_update_tracking;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.server.level.ChunkHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChunkHolder.class)
public class ChunkHolderMixin {

    @Shadow
    @Final
    private ShortSet[] changedBlocksPerSection;

    @Shadow
    private boolean hasChangedSections;

    /**
     * Using Hashsets instead of ArraySets for better worst-case performance
     * The default case of just a few items may be very slightly slower
     */
    @ModifyVariable(
            method = "blockChanged",
            at = @At(
                    ordinal = 0,
                    value = "FIELD",
                    target = "Lnet/minecraft/server/level/ChunkHolder;changedBlocksPerSection:[Lit/unimi/dsi/fastutil/shorts/ShortSet;",
                    shift = At.Shift.BEFORE
            )
    )
    private byte battery$createShortHashSet(byte b) {
        if (changedBlocksPerSection[b] == null) {
            this.hasChangedSections = true;
            this.changedBlocksPerSection[b] = new ShortOpenHashSet();
        }
        return b;
    }
}
