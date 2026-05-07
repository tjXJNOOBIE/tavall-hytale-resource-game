package com.tavall.hytale.resourcegame.liveops.config;

import java.util.Objects;

public final class SystemToggleHandler {
    private final LiveConfigRegistry registry;

    public SystemToggleHandler(LiveConfigRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public boolean isEnabled(GameSystemToggle toggle) {
        return registry.isEnabled(toggle.configKey()) && registry.getBoolean(toggle.configKey(), true);
    }

    public void requireEnabled(GameSystemToggle toggle) {
        if (!isEnabled(toggle)) {
            throw new LiveConfigDisabledException("System toggle is disabled: " + toggle.name() + " (" + toggle.configKey() + ").");
        }
    }
}
