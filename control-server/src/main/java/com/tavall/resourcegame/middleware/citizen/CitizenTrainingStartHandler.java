package com.tavall.resourcegame.middleware.citizen;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.domain.CitizenJobType;

public final class CitizenTrainingStartHandler implements ICitizenDomain, IDependencyInjectableConcrete {
    public CitizenTrainingStartHandler() {
    }

    public CitizenTrainingStartHandler(
            CitizenRepository citizenRepository,
            CitizenTrainingEligibilityHandler trainingEligibilityHandler,
            CitizenCacheInvalidationHandler cacheInvalidationHandler
    ) {
        registerCitizenDependency(CitizenRepository.class, citizenRepository);
        registerCitizenDependency(CitizenTrainingEligibilityHandler.class, trainingEligibilityHandler);
        registerCitizenDependency(CitizenCacheInvalidationHandler.class, cacheInvalidationHandler);
    }

    public CitizenData startTraining(CitizenId citizenId, long nowEpochMillis) {
        CitizenData citizen = getCitizenRepository().findCitizen(citizenId)
                .orElseThrow(() -> new CitizenValidationException("Citizen was not found."));
        getCitizenTrainingEligibilityHandler().requireCanStartTraining(citizen);
        CitizenData updated = getCitizenRepository().saveCitizen(citizen.withTraining(
                CitizenTrainingState.BASIC,
                CitizenStatus.IN_TRAINING,
                CitizenTroopLinkState.TRAINING_FOR_TROOP,
                CitizenJobType.TRAINEE,
                nowEpochMillis
        ));
        getCitizenCacheInvalidationHandler().invalidateCitizenScopes(updated);
        return updated;
    }
}
