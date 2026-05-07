package com.tavall.hytale.resourcegame.middleware.healing;

import com.tavall.hytale.resourcegame.middleware.common.CanonicalLocation;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;
import com.tavall.hytale.resourcegame.middleware.troop.InMemoryTroopRepository;
import com.tavall.hytale.resourcegame.middleware.troop.Troop;
import com.tavall.hytale.resourcegame.middleware.troop.TroopRegistrationHandler;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class TroopHealingRecipeValidationIntegrationTest {
    @Test
    void generalWoundFoodOnlyWorksAndProperTreatmentRequiresBandagePearlAndFacility() {
        Troop troop = troop(2);
        TroopWound wound = TroopWound.active(troop.troopId(), WoundType.GENERAL_WOUND, WoundSeverity.MODERATE, Instant.parse("2026-04-30T14:20:00Z"));
        TroopHealingRecipeSelectionHandler recipeSelectionHandler = new TroopHealingRecipeSelectionHandler();
        TroopHealingRecipeValidationHandler validationHandler = validationHandler();
        HealingInventory rationOnlyInventory = new HealingInventory(Map.of(HealingItemType.FIELD_RATIONS.globalAssetId(), 10));

        assertTrue(validationHandler.validateHealingRecipe(troop, wound, recipeSelectionHandler.foodOnlyRecipeFor(WoundType.GENERAL_WOUND), rationOnlyInventory, Optional.empty()).valid());
        HealingValidationResult blockedProper = validationHandler.validateHealingRecipe(troop, wound, recipeSelectionHandler.properTreatmentRecipeFor(WoundType.GENERAL_WOUND), rationOnlyInventory, facility(1));
        assertFalse(blockedProper.valid());
        assertTrue(blockedProper.missingResources().contains(HealingItemType.BANDAGE_KIT.globalAssetId()));
        assertTrue(blockedProper.missingResources().contains(GemType.PEARL.globalAssetId()));

        HealingInventory completeInventory = rationOnlyInventory
                .withAdded(HealingItemType.BANDAGE_KIT.globalAssetId(), 10)
                .withAdded(GemType.PEARL.globalAssetId(), 10);
        assertTrue(validationHandler.validateHealingRecipe(troop, wound, recipeSelectionHandler.properTreatmentRecipeFor(WoundType.GENERAL_WOUND), completeInventory, facility(1)).valid());
    }

    @Test
    void poisonAndMagicUseAmethystAndRejectMissingFacilityOrGem() {
        Troop troop = troop(5);
        TroopHealingRecipeSelectionHandler recipeSelectionHandler = new TroopHealingRecipeSelectionHandler();
        TroopHealingRecipeValidationHandler validationHandler = validationHandler();
        HealingInventory inventory = new HealingInventory(Map.of())
                .withAdded(HealingItemType.FIELD_RATIONS.globalAssetId(), 20)
                .withAdded(HealingItemType.ANTIDOTE_KIT.globalAssetId(), 5)
                .withAdded(HealingItemType.ARCANE_SALVE.globalAssetId(), 5);
        TroopWound poison = TroopWound.active(troop.troopId(), WoundType.POISONED, WoundSeverity.SEVERE, Instant.parse("2026-04-30T14:25:00Z"));
        TroopWound magic = TroopWound.active(troop.troopId(), WoundType.MAGIC_WOUND, WoundSeverity.SEVERE, Instant.parse("2026-04-30T14:26:00Z"));

        assertTrue(validationHandler.validateHealingRecipe(troop, poison, recipeSelectionHandler.foodOnlyRecipeFor(WoundType.POISONED), inventory, Optional.empty()).valid());
        assertFalse(validationHandler.validateHealingRecipe(troop, poison, recipeSelectionHandler.properTreatmentRecipeFor(WoundType.POISONED), inventory, facility(12)).valid());
        HealingInventory withAmethyst = inventory.withAdded(GemType.AMETHYST.globalAssetId(), 5);
        assertTrue(validationHandler.validateHealingRecipe(troop, poison, recipeSelectionHandler.properTreatmentRecipeFor(WoundType.POISONED), withAmethyst, facility(12)).valid());
        assertFalse(validationHandler.validateHealingRecipe(troop, magic, recipeSelectionHandler.properTreatmentRecipeFor(WoundType.MAGIC_WOUND), withAmethyst, facility(12)).valid());
        assertTrue(validationHandler.validateHealingRecipe(troop, magic, recipeSelectionHandler.properTreatmentRecipeFor(WoundType.MAGIC_WOUND), withAmethyst, facility(24)).valid());
    }

    @Test
    void missingFoodRejectsAllHealingAndExhaustedFoodOnlyWorksWithRations() {
        Troop troop = troop(1);
        TroopWound exhausted = TroopWound.active(troop.troopId(), WoundType.EXHAUSTED, WoundSeverity.MINOR, Instant.parse("2026-04-30T14:30:00Z"));
        TroopHealingRecipeSelectionHandler recipeSelectionHandler = new TroopHealingRecipeSelectionHandler();
        TroopHealingRecipeValidationHandler validationHandler = validationHandler();

        assertFalse(validationHandler.validateHealingRecipe(troop, exhausted, recipeSelectionHandler.foodOnlyRecipeFor(WoundType.EXHAUSTED), new HealingInventory(Map.of()), Optional.empty()).valid());
        assertTrue(validationHandler.validateHealingRecipe(troop, exhausted, recipeSelectionHandler.foodOnlyRecipeFor(WoundType.EXHAUSTED), new HealingInventory(Map.of(HealingItemType.FIELD_RATIONS.globalAssetId(), 4)), Optional.empty()).valid());
    }

    @Test
    void unsupportedFacilityLevelRejectsProperTreatmentForHighTierTroop() {
        Troop troop = troop(9);
        TroopWound wound = TroopWound.active(troop.troopId(), WoundType.GENERAL_WOUND, WoundSeverity.MINOR, Instant.parse("2026-04-30T14:35:00Z"));
        HealingInventory completeInventory = new HealingInventory(Map.of())
                .withAdded(HealingItemType.FIELD_RATIONS.globalAssetId(), 20)
                .withAdded(HealingItemType.BANDAGE_KIT.globalAssetId(), 20)
                .withAdded(GemType.PEARL.globalAssetId(), 20);

        HealingValidationResult validationResult = validationHandler().validateHealingRecipe(troop, wound, new TroopHealingRecipeSelectionHandler().properTreatmentRecipeFor(WoundType.GENERAL_WOUND), completeInventory, facility(1));

        assertFalse(validationResult.valid());
        assertTrue(validationResult.disabledReason().orElseThrow().contains("cannot properly treat"));
    }

    private Troop troop(int tier) {
        return new TroopRegistrationHandler(new InMemoryTroopRepository())
                .registerTroop(Optional.of(UniversalPlayerId.random()), Optional.empty(), "infantry", tier, new CanonicalLocation("world", 0.0d, 64.0d, 0.0d));
    }

    private Optional<HealingFacilityLevelDefinition> facility(int level) {
        return new HealingFacilityDefinitionRegistry().definitionForLevel(level);
    }

    private TroopHealingRecipeValidationHandler validationHandler() {
        HealingFacilityModifierCalculationHandler modifierCalculationHandler = new HealingFacilityModifierCalculationHandler();
        return new TroopHealingRecipeValidationHandler(new HealingResourceCostCalculationHandler(modifierCalculationHandler), modifierCalculationHandler);
    }
}
