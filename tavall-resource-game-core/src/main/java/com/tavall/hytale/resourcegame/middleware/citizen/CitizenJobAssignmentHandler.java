package com.tavall.hytale.resourcegame.middleware.citizen;

import com.tavall.hytale.resourcegame.domain.CitizenJobType;

public final class CitizenJobAssignmentHandler {
    private final CitizenRepository citizenRepository;
    private final CitizenJobEligibilityHandler jobEligibilityHandler;
    private final CitizenCacheInvalidationHandler cacheInvalidationHandler;

    public CitizenJobAssignmentHandler(
            CitizenRepository citizenRepository,
            CitizenJobEligibilityHandler jobEligibilityHandler,
            CitizenCacheInvalidationHandler cacheInvalidationHandler
    ) {
        this.citizenRepository = citizenRepository;
        this.jobEligibilityHandler = jobEligibilityHandler;
        this.cacheInvalidationHandler = cacheInvalidationHandler;
    }

    public CitizenData assignJob(CitizenId citizenId, CitizenJobType jobType, long nowEpochMillis) {
        CitizenData citizen = citizenRepository.findCitizen(citizenId)
                .orElseThrow(() -> new CitizenValidationException("Citizen was not found."));
        jobEligibilityHandler.requireEligible(citizen, jobType);
        CitizenData updated = citizenRepository.saveCitizen(citizen.withJob(jobType, nowEpochMillis));
        cacheInvalidationHandler.invalidateCitizenScopes(updated);
        return updated;
    }

    public CitizenData clearJob(CitizenId citizenId, long nowEpochMillis) {
        return assignJob(citizenId, CitizenJobType.IDLE, nowEpochMillis);
    }
}
