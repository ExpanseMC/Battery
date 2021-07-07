package com.expansemc.battery;

import com.google.inject.Inject;
import me.jellysquid.mods.lithium.common.config.BatteryConfig;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.plugin.jvm.Plugin;

@Plugin("battery")
public final class Battery {
    public static BatteryConfig CONFIG;

    private final Logger logger;

    @Inject
    public Battery(final Logger logger) {
        this.logger = logger;
    }

    @Listener
    public void onConstruct(ConstructPluginEvent event) {
        if (CONFIG == null) {
            throw new IllegalStateException("The mixin plugin did not initialize the config! Did it not load?");
        } else {
            this.logger.info("Injected FLARD-based optimizations.");
        }
    }
}