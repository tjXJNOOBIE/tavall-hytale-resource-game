package com.tavall.hytale.resourcegame.liveops.config;

import java.util.Objects;

public final class FeatureFlagHandler {
    private final LiveConfigRegistry registry;

    public FeatureFlagHandler(LiveConfigRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public boolean isEnabled(String key) {
        return registry.isEnabled(key) && registry.getBoolean(key, true);
    }
}
