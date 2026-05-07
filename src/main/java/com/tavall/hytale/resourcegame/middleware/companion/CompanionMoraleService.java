package com.tavall.hytale.resourcegame.middleware.companion;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandValidationException;

import java.util.UUID;

public final class CompanionMoraleService {
    private final CompanionRepository repository;
    private final CompanionStatsService statsService;

    public CompanionMoraleService(CompanionRepository repository, CompanionStatsService statsService) {
        this.repository = repository;
        this.statsService = statsService;
    }

    public CompanionData updateMorale(UUID companionId, CompanionMoraleState moraleState, long nowEpochMillis) {
        CompanionData companion = repository.findCompanion(companionId)
                .orElseThrow(() -> new ControlCommandValidationException("Companion was not found."));
        CompanionData updated = companion.withMorale(moraleState, companion.calculatedStats(), nowEpochMillis);
        return repository.saveCompanion(updated.withMorale(moraleState, statsService.calculateCompanionStats(updated), nowEpochMillis));
    }
}
