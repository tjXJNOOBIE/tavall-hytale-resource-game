package org.tavall.control.healing;

import org.tavall.control.troop.Troop;

import java.time.Duration;
import java.util.Optional;

public final class HealingFacilityModifierCalculationHandler {
    public Duration calculateHealingDuration(
            TroopHealingRecipe recipe,
            Troop troop,
            WoundSeverity severity,
            Optional<HealingFacilityLevelDefinition> facility
    ) {
        double tierMultiplier = 1.0d + ((troop.tier() - 1) * recipe.troopTierCostMultiplier());
        double severityMultiplier = severity.costMultiplier() * Math.max(1.0d, recipe.severityCostMultiplier());
        double speedModifier = facility
                .map(definition -> recipe.healingMode() == HealingMode.FOOD_ONLY
                        ? definition.foodOnlyHealingSpeedModifier()
                        : definition.properTreatmentSpeedModifier())
                .orElse(1.0d);
        long seconds = Math.max(60L, Math.round(recipe.baseDuration().toSeconds() * tierMultiplier * severityMultiplier / speedModifier));
        return Duration.ofSeconds(seconds);
    }

    public int calculateModifiedResourceAmount(
            int baseAmount,
            TroopHealingRecipe recipe,
            Troop troop,
            WoundSeverity severity,
            Optional<HealingFacilityLevelDefinition> facility
    ) {
        double tierMultiplier = 1.0d + ((troop.tier() - 1) * recipe.troopTierCostMultiplier());
        double facilityModifier = recipe.healingMode() == HealingMode.PROPER_TREATMENT
                ? facility.map(HealingFacilityLevelDefinition::resourceCostModifier).orElse(1.0d)
                : 1.0d;
        return Math.max(1, (int) Math.ceil(baseAmount * tierMultiplier * severity.costMultiplier() * facilityModifier));
    }
}
