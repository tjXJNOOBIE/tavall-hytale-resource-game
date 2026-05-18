package org.tavall.control.runtime;

import org.tavall.control.common.GamePlatform;

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
