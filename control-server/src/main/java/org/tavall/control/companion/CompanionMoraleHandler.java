package org.tavall.control.companion;

import org.tavall.control.runtime.ControlCommandValidationException;

import java.util.UUID;

public final class CompanionMoraleHandler implements ICompanionDomain {
    public CompanionMoraleHandler() {
    }

    public CompanionMoraleHandler(CompanionRepository repository, CompanionStatsHandler statsHandler) {
        registerCompanionRepository(repository);
        registerCompanionStatsHandler(statsHandler);
    }

    public CompanionData updateMorale(UUID companionId, CompanionMoraleState moraleState, long nowEpochMillis) {
        CompanionData companion = getCompanionRepository().findCompanion(companionId)
                .orElseThrow(() -> new ControlCommandValidationException("Companion was not found."));
        CompanionData updated = companion.withMorale(moraleState, companion.calculatedStats(), nowEpochMillis);
        return getCompanionRepository().saveCompanion(updated.withMorale(moraleState, getCompanionStatsHandler().calculateCompanionStats(updated), nowEpochMillis));
    }
}
