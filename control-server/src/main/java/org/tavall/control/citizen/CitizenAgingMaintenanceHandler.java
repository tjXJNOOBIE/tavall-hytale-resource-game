package org.tavall.control.citizen;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.ArrayList;
import java.util.List;

public final class CitizenAgingMaintenanceHandler implements CitizenDomain, IDependencyInjectableConcrete {
    public CitizenAgingMaintenanceHandler() {
    }

    public CitizenAgingMaintenanceHandler(
            CitizenRepository citizenRepository,
            CitizenAgingCalculationHandler agingCalculationHandler,
            CitizenCacheInvalidationHandler cacheInvalidationHandler
    ) {
        registerCitizenDependency(CitizenRepository.class, citizenRepository);
        registerCitizenDependency(CitizenAgingCalculationHandler.class, agingCalculationHandler);
        registerCitizenDependency(CitizenCacheInvalidationHandler.class, cacheInvalidationHandler);
    }

    public List<CitizenData> runMaintenance(String kingdomId, long nowEpochMillis, CitizenAgingConfig config) {
        if (!config.enabled()) {
            return List.of();
        }
        ArrayList<CitizenData> changed = new ArrayList<>();
        for (CitizenData citizen : getCitizenRepository().findCitizensForKingdom(kingdomId)) {
            CitizenAgeStage calculatedStage = getCitizenAgingCalculationHandler().calculateAgeStage(citizen, nowEpochMillis, config);
            if (calculatedStage != citizen.ageStage()) {
                CitizenData updated = getCitizenRepository().saveCitizen(citizen.withAgeStage(calculatedStage, nowEpochMillis));
                getCitizenCacheInvalidationHandler().invalidateCitizenScopes(updated);
                changed.add(updated);
            }
        }
        return List.copyOf(changed);
    }
}
