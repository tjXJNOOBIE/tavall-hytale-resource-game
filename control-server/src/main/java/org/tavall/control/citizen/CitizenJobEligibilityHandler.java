package org.tavall.control.citizen;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.control.domain.CitizenJobType;

public final class CitizenJobEligibilityHandler implements CitizenDomain, IDependencyInjectableConcrete {
    public CitizenJobEligibilityHandler() {
    }

    public CitizenJobEligibilityHandler(CitizenLifeStageEligibilityHandler lifeStageEligibilityHandler) {
        registerCitizenDependency(CitizenLifeStageEligibilityHandler.class, lifeStageEligibilityHandler);
    }

    public void requireEligible(CitizenData citizen, CitizenJobType jobType) {
        if (citizen.status() == CitizenStatus.DEAD || citizen.ageStage() == CitizenAgeStage.DECEASED) {
            throw new CitizenValidationException("Dead citizens cannot receive jobs.");
        }
        if (citizen.status() == CitizenStatus.ACTIVE_TROOP && jobType != CitizenJobType.SOLDIER) {
            throw new CitizenValidationException("Active troops must keep the SOLDIER job.");
        }
        if (!getCitizenLifeStageEligibilityHandler().allowedJobs(citizen.ageStage()).contains(jobType)) {
            throw new CitizenValidationException("Job " + jobType + " is not allowed for age stage " + citizen.ageStage() + ".");
        }
        if (citizen.healthState() == CitizenHealthState.CRITICAL) {
            throw new CitizenValidationException("Critical citizens cannot receive jobs.");
        }
    }
}
