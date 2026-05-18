package org.tavall.control.support;

import org.tavall.control.player.IIpHashService;

public final class NoopIpHashService implements IIpHashService {
    @Override
    public String hash(String rawValue) {
        return "";
    }
}


