package org.tavall.control.citizen;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.control.identity.UniversalPlayerId;

import java.util.List;
import java.util.Optional;

public final class CitizenReadHandler implements CitizenDomain, IDependencyInjectableConcrete {
    public CitizenReadHandler() {
    }

    public CitizenReadHandler(CitizenRepository citizenRepository, CitizenAgingCalculationHandler agingCalculationHandler) {
        registerCitizenDependency(CitizenRepository.class, citizenRepository);
        registerCitizenDependency(CitizenAgingCalculationHandler.class, agingCalculationHandler);
    }

    public Optional<CitizenData> findCitizen(CitizenId citizenId, long nowEpochMillis, CitizenAgingConfig config) {
        return getCitizenRepository().findCitizen(citizenId)
                .map(citizen -> refreshStageOnLoad(citizen, nowEpochMillis, config));
    }

    public List<CitizenData> findCitizensForPlayer(UniversalPlayerId ownerPlayerId, long nowEpochMillis, CitizenAgingConfig config) {
        return getCitizenRepository().findCitizensForPlayer(ownerPlayerId).stream()
                .map(citizen -> refreshStageOnLoad(citizen, nowEpochMillis, config))
                .toList();
    }

    public List<CitizenData> findCitizensForKingdom(String kingdomId, long nowEpochMillis, CitizenAgingConfig config) {
        return getCitizenRepository().findCitizensForKingdom(kingdomId).stream()
                .map(citizen -> refreshStageOnLoad(citizen, nowEpochMillis, config))
                .toList();
    }

    public CitizenData refreshStageOnLoad(CitizenData citizen, long nowEpochMillis, CitizenAgingConfig config) {
        CitizenAgeStage calculatedStage = getCitizenAgingCalculationHandler().calculateAgeStage(citizen, nowEpochMillis, config);
        if (calculatedStage == citizen.ageStage()) {
            return citizen;
        }
        return getCitizenRepository().saveCitizen(citizen.withAgeStage(calculatedStage, nowEpochMillis));
    }
}
