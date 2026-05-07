package com.tavall.hytale.resourcegame.middleware.citizen;

public final class CitizenConditionUpdateHandler {
    private final CitizenRepository citizenRepository;
    private final CitizenCacheInvalidationHandler cacheInvalidationHandler;

    public CitizenConditionUpdateHandler(CitizenRepository citizenRepository, CitizenCacheInvalidationHandler cacheInvalidationHandler) {
        this.citizenRepository = citizenRepository;
        this.cacheInvalidationHandler = cacheInvalidationHandler;
    }

    public CitizenData updateConditions(
            CitizenId citizenId,
            CitizenHealthState healthState,
            CitizenMoraleState moraleState,
            CitizenNutritionState nutritionState,
            CitizenHousingState housingState,
            long nowEpochMillis
    ) {
        CitizenData citizen = citizenRepository.findCitizen(citizenId)
                .orElseThrow(() -> new CitizenValidationException("Citizen was not found."));
        CitizenData updated = citizenRepository.saveCitizen(citizen.withConditions(
                healthState == null ? citizen.healthState() : healthState,
                moraleState == null ? citizen.moraleState() : moraleState,
                nutritionState == null ? citizen.nutritionState() : nutritionState,
                housingState == null ? citizen.housingState() : housingState,
                nowEpochMillis
        ));
        cacheInvalidationHandler.invalidateCitizenScopes(updated);
        return updated;
    }
}
