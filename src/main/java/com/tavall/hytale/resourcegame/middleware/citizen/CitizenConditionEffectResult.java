package com.tavall.hytale.resourcegame.middleware.citizen;

public record CitizenConditionEffectResult(
        CitizenData citizen,
        double workEfficiencyModifier,
        double trainingModifier,
        String reason
) {
}
