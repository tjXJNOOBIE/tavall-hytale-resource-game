package org.tavall.control.companion;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record CompanionData(
        UUID companionId,
        UUID ownerPlayerId,
        CompanionType type,
        CompanionStatus status,
        CompanionBehaviorState behaviorState,
        CompanionMoraleState moraleState,
        int level,
        long xp,
        long createdAtEpochMillis,
        long updatedAtEpochMillis,
        CompanionBaseAttributes baseAttributes,
        CompanionStats calculatedStats,
        List<CompanionSkillSlot> skillSlots,
        Optional<UUID> activeSkinId,
        Optional<String> activeWallSectionId,
        Map<String, String> metadata
) {
    public static final int MAX_LEVEL = 70;

    public CompanionData {
        skillSlots = List.copyOf(skillSlots == null ? List.of() : skillSlots);
        activeSkinId = activeSkinId == null ? Optional.empty() : activeSkinId;
        activeWallSectionId = activeWallSectionId == null ? Optional.empty() : activeWallSectionId;
        metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
        if (level < 1 || level > MAX_LEVEL) {
            throw new IllegalArgumentException("Companion level must be between 1 and 70.");
        }
        if (xp < 0) {
            throw new IllegalArgumentException("Companion XP cannot be negative.");
        }
    }

    public CompanionData withProgress(int newLevel, long newXp, CompanionStats stats, long nowEpochMillis) {
        return new CompanionData(companionId, ownerPlayerId, type, status, behaviorState, moraleState, newLevel, newXp, createdAtEpochMillis, nowEpochMillis, baseAttributes, stats, skillSlots, activeSkinId, activeWallSectionId, metadata);
    }

    public CompanionData withSkillSlots(List<CompanionSkillSlot> slots, long nowEpochMillis) {
        return new CompanionData(companionId, ownerPlayerId, type, status, behaviorState, moraleState, level, xp, createdAtEpochMillis, nowEpochMillis, baseAttributes, calculatedStats, slots, activeSkinId, activeWallSectionId, metadata);
    }

    public CompanionData withStatusAndBehavior(CompanionStatus newStatus, CompanionBehaviorState newBehavior, long nowEpochMillis) {
        return new CompanionData(companionId, ownerPlayerId, type, newStatus, newBehavior, moraleState, level, xp, createdAtEpochMillis, nowEpochMillis, baseAttributes, calculatedStats, skillSlots, activeSkinId, activeWallSectionId, metadata);
    }

    public CompanionData withMorale(CompanionMoraleState newMorale, CompanionStats stats, long nowEpochMillis) {
        return new CompanionData(companionId, ownerPlayerId, type, status, behaviorState, newMorale, level, xp, createdAtEpochMillis, nowEpochMillis, baseAttributes, stats, skillSlots, activeSkinId, activeWallSectionId, metadata);
    }

    public CompanionData withWallSection(Optional<String> wallSectionId, CompanionStatus newStatus, long nowEpochMillis) {
        return new CompanionData(companionId, ownerPlayerId, type, newStatus, behaviorState, moraleState, level, xp, createdAtEpochMillis, nowEpochMillis, baseAttributes, calculatedStats, skillSlots, activeSkinId, wallSectionId, metadata);
    }
}
