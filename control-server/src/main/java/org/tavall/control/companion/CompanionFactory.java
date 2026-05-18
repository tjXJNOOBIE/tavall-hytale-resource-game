package org.tavall.control.companion;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class CompanionFactory implements ICompanionDomain {
    public CompanionFactory() {
    }

    public CompanionFactory(CompanionStatsHandler statsHandler) {
        registerCompanionStatsHandler(statsHandler);
    }

    public CompanionData createCompanion(UUID ownerPlayerId, CompanionType type, long nowEpochMillis) {
        CompanionBaseAttributes attributes = baseAttributes(type);
        CompanionData draft = new CompanionData(
                UUID.randomUUID(),
                ownerPlayerId,
                type,
                CompanionStatus.IDLE,
                CompanionBehaviorState.IDLE,
                CompanionMoraleState.MEDIUM,
                1,
                0,
                nowEpochMillis,
                nowEpochMillis,
                attributes,
                new CompanionStats(0, 0, 0, 0, 0, 0, 0, 0),
                defaultSlots(),
                Optional.empty(),
                Optional.empty(),
                Map.of("createdBy", "companion-factory")
        );
        return draft.withProgress(1, 0, getCompanionStatsHandler().calculateCompanionStats(draft), nowEpochMillis);
    }

    public List<CompanionSkillSlot> defaultSlots() {
        return List.of(
                new CompanionSkillSlot(1, Optional.empty(), true, 1),
                new CompanionSkillSlot(2, Optional.empty(), false, 10),
                new CompanionSkillSlot(3, Optional.empty(), false, 30),
                new CompanionSkillSlot(4, Optional.empty(), false, 50)
        );
    }

    private CompanionBaseAttributes baseAttributes(CompanionType type) {
        return switch (type) {
            case HEALER -> new CompanionBaseAttributes(12, 5, 7);
            case BRAWLER -> new CompanionBaseAttributes(5, 10, 11);
            case BRUTE -> new CompanionBaseAttributes(4, 14, 5);
            case ARCANE -> new CompanionBaseAttributes(14, 4, 8);
        };
    }
}
