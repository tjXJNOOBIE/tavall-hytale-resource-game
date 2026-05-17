package com.tavall.resourcegame.middleware.citizen;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.domain.CitizenJobType;

import java.util.EnumSet;
import java.util.Set;

public final class CitizenLifeStageEligibilityHandler implements ICitizenDomain, IDependencyInjectableConcrete {
    public boolean canWork(CitizenAgeStage ageStage) {
        return switch (ageStage) {
            case TEEN, YOUNG_ADULT, ADULT, MIDDLE_AGED, ELDER -> true;
            case INFANT, CHILD, DECEASED -> false;
        };
    }

    public boolean canTrain(CitizenAgeStage ageStage) {
        return switch (ageStage) {
            case TEEN, YOUNG_ADULT, ADULT, MIDDLE_AGED -> true;
            case INFANT, CHILD, ELDER, DECEASED -> false;
        };
    }

    public boolean canPromoteToTroop(CitizenAgeStage ageStage) {
        return switch (ageStage) {
            case YOUNG_ADULT, ADULT, MIDDLE_AGED -> true;
            case INFANT, CHILD, TEEN, ELDER, DECEASED -> false;
        };
    }

    public Set<CitizenJobType> allowedJobs(CitizenAgeStage ageStage) {
        return switch (ageStage) {
            case INFANT, CHILD, DECEASED -> EnumSet.of(CitizenJobType.IDLE);
            case TEEN -> EnumSet.of(CitizenJobType.IDLE, CitizenJobType.GATHERER, CitizenJobType.COOK, CitizenJobType.TRAINEE);
            case ELDER -> EnumSet.of(CitizenJobType.IDLE, CitizenJobType.COOK, CitizenJobType.ARCHITECT);
            case YOUNG_ADULT, ADULT, MIDDLE_AGED -> EnumSet.allOf(CitizenJobType.class);
        };
    }
}
