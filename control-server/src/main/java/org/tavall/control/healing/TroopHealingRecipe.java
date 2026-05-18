package org.tavall.control.healing;

import org.tavall.control.common.MetadataMaps;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record TroopHealingRecipe(
        String recipeId,
        WoundType woundType,
        HealingMode healingMode,
        List<HealingResourceStack> requiredFoodItems,
        List<HealingResourceStack> requiredHealingItems,
        Set<GemType> requiredGemTypes,
        int requiredFacilityLevel,
        Duration baseDuration,
        double troopTierCostMultiplier,
        double severityCostMultiplier,
        Map<String, String> metadata
) {
    public TroopHealingRecipe {
        if (recipeId == null || recipeId.isBlank()) {
            throw new IllegalArgumentException("recipeId is required.");
        }
        Objects.requireNonNull(woundType, "woundType");
        Objects.requireNonNull(healingMode, "healingMode");
        requiredFoodItems = requiredFoodItems == null ? List.of() : List.copyOf(requiredFoodItems);
        requiredHealingItems = requiredHealingItems == null ? List.of() : List.copyOf(requiredHealingItems);
        requiredGemTypes = requiredGemTypes == null ? Set.of() : Set.copyOf(requiredGemTypes);
        requiredFacilityLevel = Math.max(0, requiredFacilityLevel);
        baseDuration = baseDuration == null ? Duration.ofHours(1L) : baseDuration;
        troopTierCostMultiplier = Math.max(0.0d, troopTierCostMultiplier);
        severityCostMultiplier = Math.max(0.0d, severityCostMultiplier);
        metadata = MetadataMaps.immutable(metadata);
    }
}
