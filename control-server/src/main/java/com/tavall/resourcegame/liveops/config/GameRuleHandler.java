package com.tavall.resourcegame.liveops.config;

import com.tavall.resourcegame.liveops.ILiveOpsDomain;

public final class GameRuleHandler implements ILiveOpsDomain {
    public boolean isEnabled(String key) {
        return getLiveConfigRegistry().isEnabled(key);
    }

    public void requireEnabled(String key) {
        if (!isEnabled(key)) {
            throw new LiveConfigDisabledException("Game rule is disabled: " + key + ".");
        }
    }

    public int getInt(String key, int fallback) {
        return getLiveConfigRegistry().getInt(key, fallback);
    }

    public double getDouble(String key, double fallback) {
        return getLiveConfigRegistry().getDouble(key, fallback);
    }

    public String getString(String key, String fallback) {
        return getLiveConfigRegistry().getString(key, fallback);
    }
}
