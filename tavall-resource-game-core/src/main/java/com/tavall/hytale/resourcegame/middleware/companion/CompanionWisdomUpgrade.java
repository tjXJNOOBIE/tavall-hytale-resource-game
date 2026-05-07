package com.tavall.hytale.resourcegame.middleware.companion;

import java.util.Map;
import java.util.UUID;

public record CompanionWisdomUpgrade(
        UUID companionId,
        UUID skillId,
        int skillLevel,
        double cooldownModifier,
        double powerModifier,
        long updatedAtEpochMillis,
        Map<String, String> metadata
) {
    public CompanionWisdomUpgrade {
        metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
    }
}
