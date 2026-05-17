package com.tavall.resourcegame.middleware.companion;

import java.util.Map;
import java.util.UUID;

public record CompanionWallAssignment(
        UUID ownerPlayerId,
        UUID companionId,
        String wallSectionId,
        double defenseBonus,
        long updatedAtEpochMillis,
        Map<String, String> metadata
) {
    public CompanionWallAssignment {
        metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
    }
}
