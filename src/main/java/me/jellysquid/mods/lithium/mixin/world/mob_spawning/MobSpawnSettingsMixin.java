package me.jellysquid.mods.lithium.mixin.world.mob_spawning;

import com.google.common.collect.Maps;
import me.jellysquid.mods.lithium.common.util.collections.HashedReferenceList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(MobSpawnSettings.class)
public class MobSpawnSettingsMixin {
    @Mutable
    @Shadow
    @Final
    private Map<MobCategory, List<MobSpawnSettings.SpawnerData>> spawners;

    /**
     * Re-initialize the spawn category lists with a much faster backing collection type for enum keys. This provides
     * a modest speed-up for mob spawning as {@link MobSpawnSettings#getMobs(MobCategory)} is a rather hot method.
     * <p>
     * Additionally, the list containing each spawn entry is modified to include a hash table for lookups, making them
     * O(1) instead of O(n) and providing another boost when lists get large. Since a simple wrapper type is used, this
     * should provide good compatibility with other mods which modify spawn entries.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void battery$reinit(float creatureSpawnProbability, Map<MobCategory, List<MobSpawnSettings.SpawnerData>> spawners, Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> spawnCosts, boolean playerSpawnFriendly, CallbackInfo ci) {
        Map<MobCategory, List<MobSpawnSettings.SpawnerData>> spawns = Maps.newEnumMap(MobCategory.class);

        for (Map.Entry<MobCategory, List<MobSpawnSettings.SpawnerData>> entry : this.spawners.entrySet()) {
            spawns.put(entry.getKey(), new HashedReferenceList<>(entry.getValue()));
        }

        this.spawners = spawns;
    }
}
