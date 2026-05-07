package com.tavall.hytale.resourcegame.liveops.config;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record LiveConfigEntry(
        UUID configId,
        String key,
        LiveConfigType type,
        String valueJson,
        boolean enabled,
        String environment,
        long version,
        String updatedBy,
        Instant updatedAt,
        String description,
        LiveConfigRolloutStrategy rolloutStrategy
) {
    public LiveConfigEntry {
        configId = configId == null ? UUID.randomUUID() : configId;
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key is required.");
        }
        Objects.requireNonNull(type, "type");
        valueJson = valueJson == null || valueJson.isBlank() ? "null" : valueJson;
        environment = environment == null || environment.isBlank() ? "local" : environment;
        version = Math.max(1L, version);
        updatedBy = updatedBy == null || updatedBy.isBlank() ? "system" : updatedBy;
        updatedAt = updatedAt == null ? Instant.now() : updatedAt;
        description = description == null ? "" : description;
        rolloutStrategy = rolloutStrategy == null ? LiveConfigRolloutStrategy.global() : rolloutStrategy;
    }

    public LiveConfigEntry withVersionedValue(String nextValueJson, boolean nextEnabled, String nextUpdatedBy, Instant now) {
        return new LiveConfigEntry(
                configId,
                key,
                type,
                nextValueJson,
                nextEnabled,
                environment,
                version + 1,
                nextUpdatedBy,
                now,
                description,
                rolloutStrategy
        );
    }

    public LiveConfigEntry withVersionedChange(
            LiveConfigType nextType,
            String nextValueJson,
            boolean nextEnabled,
            String nextUpdatedBy,
            Instant now,
            String nextDescription,
            LiveConfigRolloutStrategy nextRolloutStrategy
    ) {
        return new LiveConfigEntry(
                configId,
                key,
                nextType,
                nextValueJson,
                nextEnabled,
                environment,
                version + 1,
                nextUpdatedBy,
                now,
                nextDescription,
                nextRolloutStrategy
        );
    }
}
