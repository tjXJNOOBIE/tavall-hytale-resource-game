package org.tavall.control.liveops.config;

import org.tavall.control.liveops.LiveOpsDomain;

public final class FeatureFlagHandler implements LiveOpsDomain {
    public boolean isEnabled(String key) {
        return getLiveConfigRegistry().isEnabled(key) && getLiveConfigRegistry().getBoolean(key, true);
    }
}
