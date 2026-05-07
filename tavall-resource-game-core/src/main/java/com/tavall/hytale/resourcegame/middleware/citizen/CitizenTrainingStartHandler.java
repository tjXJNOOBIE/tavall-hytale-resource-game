package com.tavall.hytale.resourcegame.middleware.citizen;

import com.tavall.hytale.resourcegame.domain.CitizenJobType;

public final class CitizenTrainingStartHandler {
    private final CitizenRepository citizenRepository;
    private final CitizenTrainingEligibilityHandler trainingEligibilityHandler;
    private final CitizenCacheInvalidationHandler cacheInvalidationHandler;

    public CitizenTrainingStartHandler(
            CitizenRepository citizenRepository,
            CitizenTrainingEligibilityHandler trainingEligibilityHandler,
            CitizenCacheInvalidationHandler cacheInvalidationHandler
    ) {
        this.citizenRepository = citizenRepository;
        this.trainingEligibilityHandler = trainingEligibilityHandler;
        this.cacheInvalidationHandler = cacheInvalidationHandler;
    }

    public CitizenData startTraining(CitizenId citizenId, long nowEpochMillis) {
        CitizenData citizen = citizenRepository.findCitizen(citizenId)
                .orElseThrow(() -> new CitizenValidationException("Citizen was not found."));
        trainingEligibilityHandler.requireCanStartTraining(citizen);
        CitizenData updated = citizenRepository.saveCitizen(citizen.withTraining(
                CitizenTrainingState.BASIC,
                CitizenStatus.IN_TRAINING,
                CitizenTroopLinkState.TRAINING_FOR_TROOP,
                CitizenJobType.TRAINEE,
                nowEpochMillis
        ));
        cacheInvalidationHandler.invalidateCitizenScopes(updated);
        return updated;
    }
}
