package com.tavall.hytale.resourcegame.middleware.citizen;

import com.tavall.hytale.resourcegame.domain.CitizenJobType;

public final class CitizenTroopPromotionHandler {
    private final CitizenRepository citizenRepository;
    private final CitizenTrainingEligibilityHandler trainingEligibilityHandler;
    private final CitizenCacheInvalidationHandler cacheInvalidationHandler;

    public CitizenTroopPromotionHandler(
            CitizenRepository citizenRepository,
            CitizenTrainingEligibilityHandler trainingEligibilityHandler,
            CitizenCacheInvalidationHandler cacheInvalidationHandler
    ) {
        this.citizenRepository = citizenRepository;
        this.trainingEligibilityHandler = trainingEligibilityHandler;
        this.cacheInvalidationHandler = cacheInvalidationHandler;
    }

    public CitizenData promoteToTroop(CitizenId citizenId, long nowEpochMillis) {
        CitizenData citizen = citizenRepository.findCitizen(citizenId)
                .orElseThrow(() -> new CitizenValidationException("Citizen was not found."));
        trainingEligibilityHandler.requireCanPromote(citizen);
        CitizenData updated = citizenRepository.saveCitizen(citizen.withTraining(
                CitizenTrainingState.MILITIA_READY,
                CitizenStatus.ACTIVE_TROOP,
                CitizenTroopLinkState.ACTIVE_TROOP,
                CitizenJobType.SOLDIER,
                nowEpochMillis
        ));
        cacheInvalidationHandler.invalidateCitizenScopes(updated);
        return updated;
    }
}
