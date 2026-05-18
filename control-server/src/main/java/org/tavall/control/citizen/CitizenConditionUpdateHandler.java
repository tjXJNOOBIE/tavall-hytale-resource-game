package org.tavall.control.citizen;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

public final class CitizenConditionUpdateHandler implements CitizenDomain, IDependencyInjectableConcrete {
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
