package com.tavall.resourcegame.support;

import com.tavall.resourcegame.dependency.interfaces.IIpHashService;

public final class NoopIpHashService implements IIpHashService {
    @Override
    public String hash(String rawValue) {
        return "";
    }
}

