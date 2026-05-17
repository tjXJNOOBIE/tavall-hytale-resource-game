package com.tavall.resourcegame.middleware.citizen;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;

public final class CitizenAgingCalculationHandler implements ICitizenDomain, IDependencyInjectableConcrete {
    public CitizenAgingCalculationHandler() {
    }

    public CitizenAgingCalculationHandler(CitizenAgeStageMappingHandler ageStageMappingHandler) {
        registerCitizenDependency(CitizenAgeStageMappingHandler.class, ageStageMappingHandler);
    }

    public double calculateGameYears(CitizenData citizen, long nowEpochMillis, CitizenAgingConfig config) {
        long elapsedMillis = Math.max(0, nowEpochMillis - citizen.bornAtEpochMillis());
        double gameDays = elapsedMillis / (double) config.realMillisPerGameDay();
        return gameDays / config.gameDaysPerYear();
    }

    public CitizenAgeStage calculateAgeStage(CitizenData citizen, long nowEpochMillis, CitizenAgingConfig config) {
        if (!config.enabled() || citizen.ageStage() == CitizenAgeStage.DECEASED) {
            return citizen.ageStage();
        }
        return getCitizenAgeStageMappingHandler().ageStageForYears(calculateGameYears(citizen, nowEpochMillis, config), config);
    }
}
