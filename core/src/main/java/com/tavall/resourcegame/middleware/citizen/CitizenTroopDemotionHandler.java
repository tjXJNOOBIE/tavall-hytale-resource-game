package com.tavall.resourcegame.middleware.citizen;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.domain.CitizenJobType;

public final class CitizenTroopDemotionHandler implements ICitizenDomain, IDependencyInjectableConcrete {
    public CitizenTroopDemotionHandler() {
    }

    public CitizenTroopDemotionHandler(CitizenRepository citizenRepository, CitizenCacheInvalidationHandler cacheInvalidationHandler) {
        registerCitizenDependency(CitizenRepository.class, citizenRepository);
        registerCitizenDependency(CitizenCacheInvalidationHandler.class, cacheInvalidationHandler);
    }

    public CitizenData demoteToCitizen(CitizenId citizenId, long nowEpochMillis) {
        CitizenData citizen = getCitizenRepository().findCitizen(citizenId)
                .orElseThrow(() -> new CitizenValidationException("Citizen was not found."));
        if (citizen.status() != CitizenStatus.ACTIVE_TROOP) {
            throw new CitizenValidationException("Only active troops can be demoted.");
        }
        CitizenData updated = getCitizenRepository().saveCitizen(citizen.withTraining(
                CitizenTrainingState.BASIC,
                CitizenStatus.ACTIVE_CITIZEN,
                CitizenTroopLinkState.DEMOTED_FROM_TROOP,
                CitizenJobType.IDLE,
                nowEpochMillis
        ));
        getCitizenCacheInvalidationHandler().invalidateCitizenScopes(updated);
        return updated;
    }
}
