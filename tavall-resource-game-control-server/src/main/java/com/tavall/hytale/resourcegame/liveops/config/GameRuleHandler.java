package com.tavall.hytale.resourcegame.liveops.config;

import java.util.Objects;

public final class GameRuleHandler {
    private final LiveConfigRegistry registry;

    public GameRuleHandler(LiveConfigRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public boolean isEnabled(String key) {
        return registry.isEnabled(key);
    }

    public void requireEnabled(String key) {
        if (!isEnabled(key)) {
            throw new LiveConfigDisabledException("Game rule is disabled: " + key + ".");
        }
    }

    public int getInt(String key, int fallback) {
        return registry.getInt(key, fallback);
    }

    public double getDouble(String key, double fallback) {
        return registry.getDouble(key, fallback);
    }

    public String getString(String key, String fallback) {
        return registry.getString(key, fallback);
    }
}
