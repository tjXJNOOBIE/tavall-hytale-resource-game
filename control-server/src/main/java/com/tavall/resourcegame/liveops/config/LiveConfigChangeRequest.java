package com.tavall.resourcegame.liveops.config;

import java.util.Map;

public record LiveConfigChangeRequest(
        String key,
        LiveConfigType type,
        String valueJson,
        boolean enabled,
        String environment,
        String updatedBy,
        String description,
        LiveConfigRolloutStrategy rolloutStrategy,
        Map<String, String> metadata
) {
    public LiveConfigChangeRequest {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key is required.");
        }
        if (type == null) {
            throw new IllegalArgumentException("type is required.");
        }
        valueJson = valueJson == null || valueJson.isBlank() ? "null" : valueJson;
        environment = environment == null || environment.isBlank() ? "local" : environment;
        updatedBy = updatedBy == null || updatedBy.isBlank() ? "system" : updatedBy;
        description = description == null ? "" : description;
        rolloutStrategy = rolloutStrategy == null ? LiveConfigRolloutStrategy.global() : rolloutStrategy;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
