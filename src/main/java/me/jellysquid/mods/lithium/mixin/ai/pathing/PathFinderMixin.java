package me.jellysquid.mods.lithium.mixin.ai.pathing;

import me.jellysquid.mods.lithium.common.ai.pathing.PathNodeCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.Target;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(PathFinder.class)
public class PathFinderMixin {
    @Inject(method = "findPath(Lnet/minecraft/world/level/pathfinder/Node;Ljava/util/Map;FIF)Lnet/minecraft/world/level/pathfinder/Path;", at = @At("HEAD"))
    private void battery$preFindPathToAny(Node startNode, Map<Target, BlockPos> positions, float followRange, int distance, float rangeMultiplier, CallbackInfoReturnable<Path> cir) {
        PathNodeCache.enableChunkCache();
    }

    @Inject(method = "findPath(Lnet/minecraft/world/level/pathfinder/Node;Ljava/util/Map;FIF)Lnet/minecraft/world/level/pathfinder/Path;", at = @At("RETURN"))
    private void battery$postFindPathToAny(Node startNode, Map<Target, BlockPos> positions, float followRange, int distance, float rangeMultiplier, CallbackInfoReturnable<Path> cir) {
        PathNodeCache.disableChunkCache();
    }
}
