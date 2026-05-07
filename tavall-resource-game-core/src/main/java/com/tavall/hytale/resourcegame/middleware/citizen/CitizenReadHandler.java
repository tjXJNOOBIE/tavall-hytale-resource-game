package com.tavall.hytale.resourcegame.middleware.citizen;

import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.util.List;
import java.util.Optional;

public final class CitizenReadHandler {
    private final CitizenRepository citizenRepository;
    private final CitizenAgingCalculationHandler agingCalculationHandler;

    public CitizenReadHandler(CitizenRepository citizenRepository, CitizenAgingCalculationHandler agingCalculationHandler) {
        this.citizenRepository = citizenRepository;
        this.agingCalculationHandler = agingCalculationHandler;
    }

    public Optional<CitizenData> findCitizen(CitizenId citizenId, long nowEpochMillis, CitizenAgingConfig config) {
        return citizenRepository.findCitizen(citizenId)
                .map(citizen -> refreshStageOnLoad(citizen, nowEpochMillis, config));
    }

    public List<CitizenData> findCitizensForPlayer(UniversalPlayerId ownerPlayerId, long nowEpochMillis, CitizenAgingConfig config) {
        return citizenRepository.findCitizensForPlayer(ownerPlayerId).stream()
                .map(citizen -> refreshStageOnLoad(citizen, nowEpochMillis, config))
                .toList();
    }

    public List<CitizenData> findCitizensForKingdom(String kingdomId, long nowEpochMillis, CitizenAgingConfig config) {
        return citizenRepository.findCitizensForKingdom(kingdomId).stream()
                .map(citizen -> refreshStageOnLoad(citizen, nowEpochMillis, config))
                .toList();
    }

    public CitizenData refreshStageOnLoad(CitizenData citizen, long nowEpochMillis, CitizenAgingConfig config) {
        CitizenAgeStage calculatedStage = agingCalculationHandler.calculateAgeStage(citizen, nowEpochMillis, config);
        if (calculatedStage == citizen.ageStage()) {
            return citizen;
        }
        return citizenRepository.saveCitizen(citizen.withAgeStage(calculatedStage, nowEpochMillis));
    }
}
