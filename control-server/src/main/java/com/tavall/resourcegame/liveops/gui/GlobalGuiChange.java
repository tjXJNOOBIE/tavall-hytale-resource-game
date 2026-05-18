package org.tavall.control.liveops.gui;

import java.time.Instant;
import java.util.Map;

public record GlobalGuiChange(
        GlobalGuiDefinition definition,
        Instant changedAt,
        Map<String, String> metadata
) {
    public GlobalGuiChange {
        changedAt = changedAt == null ? Instant.now() : changedAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
