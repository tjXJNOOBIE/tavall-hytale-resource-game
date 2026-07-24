package org.tavall.control.citizen;

import org.tavall.dependency.IDependencyInjectableConcrete;

public final class CitizenAgeStageMappingHandler implements CitizenDomain, IDependencyInjectableConcrete {
    public CitizenAgeStage ageStageForYears(double gameYears, CitizenAgingConfig config) {
        if (gameYears < config.ageStageThresholds().get(CitizenAgeStage.CHILD)) {
            return CitizenAgeStage.INFANT;
        }
        if (gameYears < config.ageStageThresholds().get(CitizenAgeStage.TEEN)) {
            return CitizenAgeStage.CHILD;
        }
        if (gameYears < config.ageStageThresholds().get(CitizenAgeStage.YOUNG_ADULT)) {
            return CitizenAgeStage.TEEN;
        }
        if (gameYears < config.ageStageThresholds().get(CitizenAgeStage.ADULT)) {
            return CitizenAgeStage.YOUNG_ADULT;
        }
        if (gameYears < config.ageStageThresholds().get(CitizenAgeStage.MIDDLE_AGED)) {
            return CitizenAgeStage.ADULT;
        }
        if (gameYears < config.ageStageThresholds().get(CitizenAgeStage.ELDER)) {
            return CitizenAgeStage.MIDDLE_AGED;
        }
        return CitizenAgeStage.ELDER;
    }
}
