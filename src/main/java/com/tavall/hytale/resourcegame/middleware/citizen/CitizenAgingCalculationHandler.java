package com.tavall.hytale.resourcegame.middleware.citizen;

public final class CitizenAgingCalculationHandler {
    private final CitizenAgeStageMappingHandler ageStageMappingHandler;

    public CitizenAgingCalculationHandler(CitizenAgeStageMappingHandler ageStageMappingHandler) {
        this.ageStageMappingHandler = ageStageMappingHandler;
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
        return ageStageMappingHandler.ageStageForYears(calculateGameYears(citizen, nowEpochMillis, config), config);
    }
}
