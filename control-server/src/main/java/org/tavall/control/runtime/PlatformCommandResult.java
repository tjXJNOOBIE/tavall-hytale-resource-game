package org.tavall.control.runtime;

import org.tavall.control.common.GamePlatform;
import org.tavall.control.common.MetadataMaps;

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
