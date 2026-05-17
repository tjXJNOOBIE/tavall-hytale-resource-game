package com.tavall.resourcegame.middleware.citizen;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.domain.CitizenJobType;

public final class CitizenJobAssignmentHandler implements ICitizenDomain, IDependencyInjectableConcrete {
    public CitizenJobAssignmentHandler() {
    }

    public CitizenJobAssignmentHandler(
            CitizenRepository citizenRepository,
            CitizenJobEligibilityHandler jobEligibilityHandler,
            CitizenCacheInvalidationHandler cacheInvalidationHandler
    ) {
        registerCitizenDependency(CitizenRepository.class, citizenRepository);
        registerCitizenDependency(CitizenJobEligibilityHandler.class, jobEligibilityHandler);
        registerCitizenDependency(CitizenCacheInvalidationHandler.class, cacheInvalidationHandler);
    }

    public CitizenData assignJob(CitizenId citizenId, CitizenJobType jobType, long nowEpochMillis) {
        CitizenData citizen = getCitizenRepository().findCitizen(citizenId)
                .orElseThrow(() -> new CitizenValidationException("Citizen was not found."));
        getCitizenJobEligibilityHandler().requireEligible(citizen, jobType);
        CitizenData updated = getCitizenRepository().saveCitizen(citizen.withJob(jobType, nowEpochMillis));
        getCitizenCacheInvalidationHandler().invalidateCitizenScopes(updated);
        return updated;
    }

    public CitizenData clearJob(CitizenId citizenId, long nowEpochMillis) {
        return assignJob(citizenId, CitizenJobType.IDLE, nowEpochMillis);
    }
}
