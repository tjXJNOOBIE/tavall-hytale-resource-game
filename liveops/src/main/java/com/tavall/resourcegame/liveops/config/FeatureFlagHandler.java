package com.tavall.resourcegame.liveops.config;

import com.tavall.resourcegame.liveops.ILiveOpsDomain;

public final class FeatureFlagHandler implements ILiveOpsDomain {
    public boolean isEnabled(String key) {
        return getLiveConfigRegistry().isEnabled(key) && getLiveConfigRegistry().getBoolean(key, true);
    }
}
