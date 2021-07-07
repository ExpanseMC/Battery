package com.expansemc.battery.mixin;

import com.expansemc.battery.Battery;
import me.jellysquid.mods.lithium.common.config.BatteryConfig;
import me.jellysquid.mods.lithium.common.config.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.File;
import java.util.List;
import java.util.Set;

public class BatteryMixinPlugin implements IMixinConfigPlugin {
    private static final String MIXIN_PACKAGE_ROOT = "me.jellysquid.mods.lithium.mixin.";

    private final Logger logger = LogManager.getLogger("Battery");

    private BatteryConfig config;

    @Override
    public void onLoad(final String mixinPackage) {
        try {
            this.config = BatteryConfig.load(new File("./config/battery.properties"));
        } catch (final Exception e) {
            throw new RuntimeException("Could not load configuration file for Battery", e);
        }

        this.logger.info("Loaded configuration file for Battery: {} options available, {} override(s) found",
                this.config.getOptionCount(), this.config.getOptionOverrideCount());

        Battery.CONFIG = this.config;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
        if (!mixinClassName.startsWith(MIXIN_PACKAGE_ROOT)) {
            this.logger.error("Expected mixin '{}' to start with package root '{}', treating as foreign and " +
                    "disabling!", mixinClassName, MIXIN_PACKAGE_ROOT);

            return false;
        }

        final String mixin = mixinClassName.substring(MIXIN_PACKAGE_ROOT.length());
        final Option option = this.config.getEffectiveOptionForMixin(mixin);

        if (option == null) {
            this.logger.error("No rules matched mixin '{}', treating as foreign and disabling!", mixin);

            return false;
        }

        if (option.isOverridden()) {
            String source = "[unknown]";

            if (option.isUserDefined()) {
                source = "user configuration";
            } else if (option.isModDefined()) {
                source = "mods [" + String.join(", ", option.getDefiningMods()) + "]";
            }

            if (option.isEnabled()) {
                this.logger.warn("Force-enabling mixin '{}' as rule '{}' (added by {}) enables it", mixin,
                        option.getName(), source);
            } else {
                this.logger.warn("Force-disabling mixin '{}' as rule '{}' (added by {}) disables it and children", mixin,
                        option.getName(), source);
            }
        }

        return option.isEnabled();
    }

    @Override
    public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {

    }
}