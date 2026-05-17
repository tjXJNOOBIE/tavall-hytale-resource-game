package com.tavall.resourcegame.middleware.companion;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CompanionOverviewProjection(
        UUID companionId,
        UUID ownerPlayerId,
        String displayName,
        CompanionType type,
        CompanionStatus status,
        CompanionBehaviorState behaviorState,
        CompanionMoraleState moraleState,
        int level,
        long xp,
        List<CompanionSkillSlot> skillSlots,
        String globalAssetId,
        List<String> availableActions,
        Map<String, String> metadata
) {
    public CompanionOverviewProjection {
        skillSlots = List.copyOf(skillSlots == null ? List.of() : skillSlots);
        availableActions = List.copyOf(availableActions == null ? List.of() : availableActions);
        metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
    }
}
