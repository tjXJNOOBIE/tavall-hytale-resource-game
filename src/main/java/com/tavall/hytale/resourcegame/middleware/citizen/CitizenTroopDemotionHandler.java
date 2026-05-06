package com.tavall.hytale.resourcegame.middleware.citizen;

import com.tavall.hytale.resourcegame.domain.CitizenJobType;

public final class CitizenTroopDemotionHandler {
    private final CitizenRepository citizenRepository;
    private final CitizenCacheInvalidationHandler cacheInvalidationHandler;

    public CitizenTroopDemotionHandler(CitizenRepository citizenRepository, CitizenCacheInvalidationHandler cacheInvalidationHandler) {
        this.citizenRepository = citizenRepository;
        this.cacheInvalidationHandler = cacheInvalidationHandler;
    }

    public CitizenData demoteToCitizen(CitizenId citizenId, long nowEpochMillis) {
        CitizenData citizen = citizenRepository.findCitizen(citizenId)
                .orElseThrow(() -> new CitizenValidationException("Citizen was not found."));
        if (citizen.status() != CitizenStatus.ACTIVE_TROOP) {
            throw new CitizenValidationException("Only active troops can be demoted.");
        }
        CitizenData updated = citizenRepository.saveCitizen(citizen.withTraining(
                CitizenTrainingState.BASIC,
                CitizenStatus.ACTIVE_CITIZEN,
                CitizenTroopLinkState.DEMOTED_FROM_TROOP,
                CitizenJobType.IDLE,
                nowEpochMillis
        ));
        cacheInvalidationHandler.invalidateCitizenScopes(updated);
        return updated;
    }
}
