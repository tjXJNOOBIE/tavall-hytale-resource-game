package org.tavall.control.liveops.config;

import org.tavall.control.liveops.ILiveOpsDomain;

public final class FeatureFlagHandler implements ILiveOpsDomain {
    public boolean isEnabled(String key) {
        return getLiveConfigRegistry().isEnabled(key) && getLiveConfigRegistry().getBoolean(key, true);
    }
}
