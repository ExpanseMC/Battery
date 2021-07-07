package me.jellysquid.mods.lithium.common.world.layer;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;

public interface CloneableContext<R extends Area> {
    BigContext<R> cloneContext();
}
