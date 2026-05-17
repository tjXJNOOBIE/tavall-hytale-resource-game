package com.tavall.resourcegame.middleware.citizen;

public record CitizenConditionEffectResult(
        CitizenData citizen,
        double workEfficiencyModifier,
        double trainingModifier,
        String reason
) {
}
