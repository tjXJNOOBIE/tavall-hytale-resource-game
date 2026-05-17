package com.tavall.resourcegame.middleware.citizen;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;

public final class CitizenConditionUpdateHandler implements ICitizenDomain, IDependencyInjectableConcrete {
    public CitizenConditionUpdateHandler() {
    }

    public CitizenConditionUpdateHandler(CitizenRepository citizenRepository, CitizenCacheInvalidationHandler cacheInvalidationHandler) {
        registerCitizenDependency(CitizenRepository.class, citizenRepository);
        registerCitizenDependency(CitizenCacheInvalidationHandler.class, cacheInvalidationHandler);
    }

    public CitizenData updateConditions(
            CitizenId citizenId,
            CitizenHealthState healthState,
            CitizenMoraleState moraleState,
            CitizenNutritionState nutritionState,
            CitizenHousingState housingState,
            long nowEpochMillis
    ) {
        CitizenData citizen = getCitizenRepository().findCitizen(citizenId)
                .orElseThrow(() -> new CitizenValidationException("Citizen was not found."));
        CitizenData updated = getCitizenRepository().saveCitizen(citizen.withConditions(
                healthState == null ? citizen.healthState() : healthState,
                moraleState == null ? citizen.moraleState() : moraleState,
                nutritionState == null ? citizen.nutritionState() : nutritionState,
                housingState == null ? citizen.housingState() : housingState,
                nowEpochMillis
        ));
        getCitizenCacheInvalidationHandler().invalidateCitizenScopes(updated);
        return updated;
    }
}
