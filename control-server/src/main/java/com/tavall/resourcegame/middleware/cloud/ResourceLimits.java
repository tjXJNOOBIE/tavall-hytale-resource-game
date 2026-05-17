package com.tavall.resourcegame.middleware.cloud;

import java.util.Map;

public record ResourceLimits(
        int cpuCores,
        int ramMb,
        int diskGb,
        Map<String, String> metadata
) {
    public ResourceLimits {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
