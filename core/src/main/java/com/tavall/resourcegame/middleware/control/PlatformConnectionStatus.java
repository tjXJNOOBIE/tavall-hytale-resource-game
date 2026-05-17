package com.tavall.resourcegame.middleware.control;

import com.tavall.resourcegame.middleware.common.GamePlatform;

import java.util.Objects;

public record PlatformConnectionStatus(
        GamePlatform platform,
        boolean connected,
        String message
) {
    public PlatformConnectionStatus {
        Objects.requireNonNull(platform, "platform");
        message = message == null ? "" : message;
    }
}
