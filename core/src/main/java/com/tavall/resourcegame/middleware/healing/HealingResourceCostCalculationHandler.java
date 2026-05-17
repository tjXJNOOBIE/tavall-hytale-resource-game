package com.tavall.resourcegame.middleware.healing;

import com.tavall.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.resourcegame.middleware.troop.Troop;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class HealingResourceCostCalculationHandler implements IHealingDomain {
    public HealingResourceCostCalculationHandler() {
    }

    public HealingResourceCostCalculationHandler(HealingFacilityModifierCalculationHandler facilityModifierCalculationHandler) {
        registerHealingFacilityModifierCalculationHandler(facilityModifierCalculationHandler);
    }

    public Map<GlobalAssetId, Integer> calculateRequiredResources(
            TroopHealingRecipe recipe,
            Troop troop,
            TroopWound wound,
            Optional<HealingFacilityLevelDefinition> facility
    ) {
        LinkedHashMap<GlobalAssetId, Integer> requiredResources = new LinkedHashMap<>();
        addScaledStacks(requiredResources, recipe.requiredFoodItems(), recipe, troop, wound, facility);
        addScaledStacks(requiredResources, recipe.requiredHealingItems(), recipe, troop, wound, facility);
        for (GemType gemType : recipe.requiredGemTypes()) {
            requiredResources.merge(gemType.globalAssetId(), 1, Integer::sum);
        }
        return Map.copyOf(requiredResources);
    }

    private void addScaledStacks(
            LinkedHashMap<GlobalAssetId, Integer> requiredResources,
            Iterable<HealingResourceStack> stacks,
            TroopHealingRecipe recipe,
            Troop troop,
            TroopWound wound,
            Optional<HealingFacilityLevelDefinition> facility
    ) {
        for (HealingResourceStack stack : stacks) {
            int amount = getHealingFacilityModifierCalculationHandler().calculateModifiedResourceAmount(stack.amount(), recipe, troop, wound.severity(), facility);
            requiredResources.merge(stack.globalAssetId(), amount, Integer::sum);
        }
    }
}
