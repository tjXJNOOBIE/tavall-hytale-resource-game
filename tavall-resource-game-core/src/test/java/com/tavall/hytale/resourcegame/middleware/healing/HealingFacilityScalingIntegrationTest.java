package com.tavall.hytale.resourcegame.middleware.healing;

import com.tavall.hytale.resourcegame.middleware.common.CanonicalLocation;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;
import com.tavall.hytale.resourcegame.middleware.troop.InMemoryTroopRepository;
import com.tavall.hytale.resourcegame.middleware.troop.Troop;
import com.tavall.hytale.resourcegame.middleware.troop.TroopRegistrationHandler;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class HealingFacilityScalingIntegrationTest {
    @Test
    void facilityLevelBandsSupportExpectedWoundTypes() {
        HealingFacilityDefinitionRegistry registry = new HealingFacilityDefinitionRegistry();

        assertTrue(registry.definitionForLevel(12).orElseThrow().supportsProperTreatment(WoundType.POISONED, 5));
        assertTrue(registry.definitionForLevel(24).orElseThrow().supportsProperTreatment(WoundType.MAGIC_WOUND, 8));
        assertTrue(registry.definitionForLevel(30).orElseThrow().supportsProperTreatment(WoundType.GENERAL_WOUND, 10));
        assertTrue(registry.definitionForLevel(30).orElseThrow().supportsProperTreatment(WoundType.EXHAUSTED, 10));
        assertEquals(HealingFacilityType.GUILD_HOSPITAL, registry.definitionForLevel(30).orElseThrow().facilityType());
    }

    @Test
    void higherFacilityLevelsReduceHealingTimeAndResourceCount() {
        Troop troop = new TroopRegistrationHandler(new InMemoryTroopRepository())
                .registerTroop(Optional.of(UniversalPlayerId.random()), Optional.empty(), "infantry", 8, new CanonicalLocation("world", 0.0d, 64.0d, 0.0d));
        TroopWound wound = TroopWound.active(troop.troopId(), WoundType.GENERAL_WOUND, WoundSeverity.CRITICAL, Instant.parse("2026-04-30T14:40:00Z"));
        TroopHealingRecipe recipe = new TroopHealingRecipeSelectionHandler().properTreatmentRecipeFor(WoundType.GENERAL_WOUND);
        HealingFacilityDefinitionRegistry registry = new HealingFacilityDefinitionRegistry();
        HealingFacilityModifierCalculationHandler modifierCalculationHandler = new HealingFacilityModifierCalculationHandler();
        HealingResourceCostCalculationHandler costCalculationHandler = new HealingResourceCostCalculationHandler(modifierCalculationHandler);

        Duration surgicalHallDuration = modifierCalculationHandler.calculateHealingDuration(recipe, troop, wound.severity(), registry.definitionForLevel(20));
        Duration guildHospitalDuration = modifierCalculationHandler.calculateHealingDuration(recipe, troop, wound.severity(), registry.definitionForLevel(30));
        Map<?, Integer> surgicalHallCost = costCalculationHandler.calculateRequiredResources(recipe, troop, wound, registry.definitionForLevel(20));
        Map<?, Integer> guildHospitalCost = costCalculationHandler.calculateRequiredResources(recipe, troop, wound, registry.definitionForLevel(30));

        assertTrue(guildHospitalDuration.compareTo(surgicalHallDuration) < 0);
        assertTrue(guildHospitalCost.values().stream().mapToInt(Integer::intValue).sum() < surgicalHallCost.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void properTreatmentIsFasterThanFoodOnlyWhenValid() {
        Troop troop = new TroopRegistrationHandler(new InMemoryTroopRepository())
                .registerTroop(Optional.of(UniversalPlayerId.random()), Optional.empty(), "infantry", 3, new CanonicalLocation("world", 0.0d, 64.0d, 0.0d));
        TroopWound wound = TroopWound.active(troop.troopId(), WoundType.GENERAL_WOUND, WoundSeverity.MODERATE, Instant.parse("2026-04-30T14:45:00Z"));
        TroopHealingRecipeSelectionHandler recipeSelectionHandler = new TroopHealingRecipeSelectionHandler();
        HealingFacilityModifierCalculationHandler modifierCalculationHandler = new HealingFacilityModifierCalculationHandler();

        Duration foodOnly = modifierCalculationHandler.calculateHealingDuration(recipeSelectionHandler.foodOnlyRecipeFor(WoundType.GENERAL_WOUND), troop, wound.severity(), Optional.empty());
        Duration proper = modifierCalculationHandler.calculateHealingDuration(recipeSelectionHandler.properTreatmentRecipeFor(WoundType.GENERAL_WOUND), troop, wound.severity(), new HealingFacilityDefinitionRegistry().definitionForLevel(4));

        assertTrue(proper.compareTo(foodOnly) < 0);
    }
}
