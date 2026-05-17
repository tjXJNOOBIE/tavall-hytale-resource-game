package com.tavall.resourcegame.middleware.companion;

import java.util.Map;
import java.util.UUID;

public record CompanionTrainingSession(
        UUID trainingSessionId,
        UUID companionId,
        UUID ownerPlayerId,
        long startedAtEpochMillis,
        long expectedCompletedAtEpochMillis,
        long expectedXp,
        boolean claimed,
        Map<String, String> metadata
) {
    public CompanionTrainingSession {
        metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
    }
}
