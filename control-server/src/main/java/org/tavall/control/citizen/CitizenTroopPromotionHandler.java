package org.tavall.control.citizen;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.control.domain.CitizenJobType;

public final class CitizenTroopPromotionHandler implements CitizenDomain, IDependencyInjectableConcrete {
    public CitizenTroopPromotionHandler() {
    }

    public CitizenTroopPromotionHandler(
            CitizenRepository citizenRepository,
            CitizenTrainingEligibilityHandler trainingEligibilityHandler,
            CitizenCacheInvalidationHandler cacheInvalidationHandler
    ) {
        registerCitizenDependency(CitizenRepository.class, citizenRepository);
        registerCitizenDependency(CitizenTrainingEligibilityHandler.class, trainingEligibilityHandler);
        registerCitizenDependency(CitizenCacheInvalidationHandler.class, cacheInvalidationHandler);
    }

    public CitizenData promoteToTroop(CitizenId citizenId, long nowEpochMillis) {
        CitizenData citizen = getCitizenRepository().findCitizen(citizenId)
                .orElseThrow(() -> new CitizenValidationException("Citizen was not found."));
        getCitizenTrainingEligibilityHandler().requireCanPromote(citizen);
        CitizenData updated = getCitizenRepository().saveCitizen(citizen.withTraining(
                CitizenTrainingState.MILITIA_READY,
                CitizenStatus.ACTIVE_TROOP,
                CitizenTroopLinkState.ACTIVE_TROOP,
                CitizenJobType.SOLDIER,
                nowEpochMillis
        ));
        getCitizenCacheInvalidationHandler().invalidateCitizenScopes(updated);
        return updated;
    }
}
