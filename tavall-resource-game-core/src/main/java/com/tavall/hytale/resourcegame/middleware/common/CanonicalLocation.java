package com.tavall.hytale.resourcegame.middleware.common;

import java.util.Map;

public record CanonicalLocation(
        String worldName,
        double x,
        double y,
        double z,
        Map<String, String> metadata
) {
    public CanonicalLocation {
        worldName = worldName == null || worldName.isBlank() ? "default" : worldName;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public CanonicalLocation(String worldName, double x, double y, double z) {
        this(worldName, x, y, z, Map.of());
    }
}
