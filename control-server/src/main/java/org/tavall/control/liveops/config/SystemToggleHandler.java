package org.tavall.control.liveops.config;

import org.tavall.control.liveops.ILiveOpsDomain;

public final class SystemToggleHandler implements ILiveOpsDomain {
    public boolean isEnabled(GameSystemToggle toggle) {
        return getLiveConfigRegistry().isEnabled(toggle.configKey()) && getLiveConfigRegistry().getBoolean(toggle.configKey(), true);
    }

    public void requireEnabled(GameSystemToggle toggle) {
        if (!isEnabled(toggle)) {
            throw new LiveConfigDisabledException("System toggle is disabled: " + toggle.name() + " (" + toggle.configKey() + ").");
        }
    }
}
