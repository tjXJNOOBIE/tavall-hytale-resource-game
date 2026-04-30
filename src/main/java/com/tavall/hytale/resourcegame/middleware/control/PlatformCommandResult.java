package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.common.MetadataMaps;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record PlatformCommandResult(
        GamePlatform platform,
        boolean success,
        String message,
        List<String> frontendEventIds,
        List<String> projectionIds,
        Map<String, String> metadata
) {
    public PlatformCommandResult {
        Objects.requireNonNull(platform, "platform");
        message = message == null ? "" : message;
        frontendEventIds = frontendEventIds == null ? List.of() : List.copyOf(frontendEventIds);
        projectionIds = projectionIds == null ? List.of() : List.copyOf(projectionIds);
        metadata = MetadataMaps.immutable(metadata);
    }
}
