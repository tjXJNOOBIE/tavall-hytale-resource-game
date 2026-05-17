package com.tavall.resourcegame.liveops.config;

import java.time.Instant;
import java.util.Map;

public record LiveConfigChange(
        LiveConfigEntry entry,
        String changeType,
        Instant changedAt,
        Map<String, String> metadata
) {
    public LiveConfigChange {
        if (changeType == null || changeType.isBlank()) {
            changeType = "upsert";
        }
        changedAt = changedAt == null ? Instant.now() : changedAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
