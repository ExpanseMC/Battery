package me.jellysquid.mods.lithium.mixin.entity.inactive_navigations;

import me.jellysquid.mods.lithium.common.entity.EntityNavigationExtended;
import me.jellysquid.mods.lithium.common.world.ServerWorldExtended;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PathNavigation.class)
public abstract class PathNavigationMixin implements EntityNavigationExtended {

    @Shadow
    @Final
    protected Level level;

    @Shadow
    protected Path path;

    private boolean canListenForBlocks = false;

    @Inject(
            method = "recomputePath()V",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;createPath(Lnet/minecraft/core/BlockPos;I)Lnet/minecraft/world/level/pathfinder/Path;",
                    shift = At.Shift.AFTER
            )
    )
    private void battery$updateListeningState(CallbackInfo ci) {
        if (this.canListenForBlocks) {
            if (this.path == null) {
                ((ServerWorldExtended) this.level).setNavigationInactive(this);
            } else {
                ((ServerWorldExtended) this.level).setNavigationActive(this);
            }
        }
    }

    @Inject(method = "moveTo(Lnet/minecraft/world/level/pathfinder/Path;D)Z", at = @At(value = "RETURN"))
    private void battery$updateListeningState2(Path path, double speed, CallbackInfoReturnable<Boolean> cir) {
        if (this.canListenForBlocks) {
            if (this.path == null) {
                ((ServerWorldExtended) this.level).setNavigationInactive(this);
            } else {
                ((ServerWorldExtended) this.level).setNavigationActive(this);
            }
        }
    }

    @Inject(method = "stop", at = @At(value = "RETURN"))
    private void battery$stopListening(CallbackInfo ci) {
        if (this.canListenForBlocks) {
            ((ServerWorldExtended) this.level).setNavigationInactive(this);
        }
    }

    @Override
    public void setRegisteredToWorld(boolean isRegistered) {
        // Drowneds are problematic. Their EntityNavigations do not register properly.
        // We make sure to not register them, when vanilla doesn't register them.
        this.canListenForBlocks = isRegistered;
    }

    @Override
    public boolean isRegisteredToWorld() {
        return this.canListenForBlocks;
    }
}
