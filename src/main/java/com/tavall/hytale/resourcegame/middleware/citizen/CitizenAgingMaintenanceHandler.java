package com.tavall.hytale.resourcegame.middleware.citizen;

import java.util.ArrayList;
import java.util.List;

public final class CitizenAgingMaintenanceHandler {
    private final CitizenRepository citizenRepository;
    private final CitizenAgingCalculationHandler agingCalculationHandler;
    private final CitizenCacheInvalidationHandler cacheInvalidationHandler;

    public CitizenAgingMaintenanceHandler(
            CitizenRepository citizenRepository,
            CitizenAgingCalculationHandler agingCalculationHandler,
            CitizenCacheInvalidationHandler cacheInvalidationHandler
    ) {
        this.citizenRepository = citizenRepository;
        this.agingCalculationHandler = agingCalculationHandler;
        this.cacheInvalidationHandler = cacheInvalidationHandler;
    }

    public List<CitizenData> runMaintenance(String kingdomId, long nowEpochMillis, CitizenAgingConfig config) {
        if (!config.enabled()) {
            return List.of();
        }
        ArrayList<CitizenData> changed = new ArrayList<>();
        for (CitizenData citizen : citizenRepository.findCitizensForKingdom(kingdomId)) {
            CitizenAgeStage calculatedStage = agingCalculationHandler.calculateAgeStage(citizen, nowEpochMillis, config);
            if (calculatedStage != citizen.ageStage()) {
                CitizenData updated = citizenRepository.saveCitizen(citizen.withAgeStage(calculatedStage, nowEpochMillis));
                cacheInvalidationHandler.invalidateCitizenScopes(updated);
                changed.add(updated);
            }
        }
        return List.copyOf(changed);
    }
}
