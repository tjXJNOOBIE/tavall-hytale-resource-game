package com.tavall.resourcegame.middleware.healing;

import com.tavall.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.resourcegame.middleware.troop.Troop;
import com.tavall.resourcegame.middleware.troop.TroopStatus;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TroopHealingRecipeValidationHandler implements IHealingDomain {
    public TroopHealingRecipeValidationHandler() {
    }

    public TroopHealingRecipeValidationHandler(
            HealingResourceCostCalculationHandler resourceCostCalculationHandler,
            HealingFacilityModifierCalculationHandler facilityModifierCalculationHandler
    ) {
        registerHealingResourceCostCalculationHandler(resourceCostCalculationHandler);
        registerHealingFacilityModifierCalculationHandler(facilityModifierCalculationHandler);
    }

    public HealingValidationResult validateHealingRecipe(
            Troop troop,
            TroopWound wound,
            TroopHealingRecipe recipe,
            HealingInventory inventory,
            Optional<HealingFacilityLevelDefinition> facility
    ) {
        if (!troop.troopId().equals(wound.troopId())) {
            throw new HealingValidationException("Wound does not belong to troop.");
        }
        HealingInventory availableInventory = inventory == null ? new HealingInventory(Map.of()) : inventory;
        Map<GlobalAssetId, Integer> requiredResources = getHealingResourceCostCalculationHandler().calculateRequiredResources(recipe, troop, wound, facility);
        List<GlobalAssetId> missingResources = requiredResources.entrySet().stream()
                .filter(entry -> availableInventory.amount(entry.getKey()) < entry.getValue())
                .map(Map.Entry::getKey)
                .toList();
        Optional<String> missingFacilityRequirement = validateFacility(troop, recipe, facility);
        ArrayList<String> disabledReasons = new ArrayList<>();
        if (!wound.active()) {
            disabledReasons.add("Wound is already healed.");
        }
        if (troop.status() == TroopStatus.DEAD || troop.status() == TroopStatus.CAPTURED) {
            disabledReasons.add("Troop cannot recover while " + troop.status().name().toLowerCase() + ".");
        }
        if (!missingResources.isEmpty()) {
            disabledReasons.add("Missing resources: " + joinAssetIds(missingResources) + ".");
        }
        missingFacilityRequirement.ifPresent(disabledReasons::add);
        Duration estimatedHealingTime = getHealingFacilityModifierCalculationHandler().calculateHealingDuration(recipe, troop, wound.severity(), facility);
        Optional<String> disabledReason = disabledReasons.isEmpty()
                ? Optional.empty()
                : Optional.of(String.join(" ", disabledReasons));
        return new HealingValidationResult(
                recipe,
                disabledReasons.isEmpty(),
                disabledReason,
                missingResources,
                missingFacilityRequirement,
                recipe.requiredGemTypes().stream().findFirst(),
                estimatedHealingTime,
                requiredResources
        );
    }

    private Optional<String> validateFacility(Troop troop, TroopHealingRecipe recipe, Optional<HealingFacilityLevelDefinition> facility) {
        if (recipe.healingMode() == HealingMode.FOOD_ONLY) {
            return Optional.empty();
        }
        if (facility.isEmpty()) {
            return Optional.of("Missing healing facility level " + recipe.requiredFacilityLevel() + " or better.");
        }
        HealingFacilityLevelDefinition definition = facility.orElseThrow();
        if (definition.buildingLevel() < recipe.requiredFacilityLevel()) {
            return Optional.of("Healing facility level " + definition.buildingLevel() + " is below required level " + recipe.requiredFacilityLevel() + ".");
        }
        if (!definition.supportsProperTreatment(recipe.woundType(), troop.tier())) {
            return Optional.of(definition.facilityName() + " cannot properly treat " + recipe.woundType().name() + " for tier " + troop.tier() + " troops.");
        }
        return Optional.empty();
    }

    private String joinAssetIds(List<GlobalAssetId> globalAssetIds) {
        return String.join(", ", globalAssetIds.stream().map(GlobalAssetId::value).toList());
    }
}
