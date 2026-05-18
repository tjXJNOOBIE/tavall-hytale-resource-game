package org.tavall.control.support;

import org.tavall.control.player.IIpHashHandler;

public final class NoopIpHashHandler implements IIpHashHandler {
    @Override
    public String hash(String rawValue) {
        return "";
    }
}


