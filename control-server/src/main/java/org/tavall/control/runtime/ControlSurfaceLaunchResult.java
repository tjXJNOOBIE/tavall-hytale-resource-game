package org.tavall.control.runtime;

import java.util.Map;

public record ControlSurfaceLaunchResult(
        boolean success,
        String message,
        String launchId,
        Map<String, String> metadata
) {
    public ControlSurfaceLaunchResult {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
