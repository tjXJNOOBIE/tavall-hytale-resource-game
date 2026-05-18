package org.tavall.control.companion;

import org.tavall.control.runtime.ControlCommandValidationException;

import java.util.UUID;

public final class CompanionMoraleService implements ICompanionDomain {
    public CompanionMoraleService() {
    }

    public CompanionMoraleService(CompanionRepository repository, CompanionStatsService statsService) {
        registerCompanionRepository(repository);
        registerCompanionStatsService(statsService);
    }

    public CompanionData updateMorale(UUID companionId, CompanionMoraleState moraleState, long nowEpochMillis) {
        CompanionData companion = getCompanionRepository().findCompanion(companionId)
                .orElseThrow(() -> new ControlCommandValidationException("Companion was not found."));
        CompanionData updated = companion.withMorale(moraleState, companion.calculatedStats(), nowEpochMillis);
        return getCompanionRepository().saveCompanion(updated.withMorale(moraleState, getCompanionStatsService().calculateCompanionStats(updated), nowEpochMillis));
    }
}
