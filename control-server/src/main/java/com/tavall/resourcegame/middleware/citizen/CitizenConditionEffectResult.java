package org.tavall.control.citizen;

public record CitizenConditionEffectResult(
        CitizenData citizen,
        double workEfficiencyModifier,
        double trainingModifier,
        String reason
) {
}
