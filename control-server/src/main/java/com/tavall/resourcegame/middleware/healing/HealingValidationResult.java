package com.tavall.resourcegame.middleware.healing;

import com.tavall.resourcegame.middleware.asset.GlobalAssetId;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record HealingValidationResult(
        TroopHealingRecipe recipe,
        boolean valid,
        Optional<String> disabledReason,
        List<GlobalAssetId> missingResources,
        Optional<String> missingFacilityRequirement,
        Optional<GemType> requiredGemType,
        Duration estimatedHealingTime,
        Map<GlobalAssetId, Integer> requiredResources
) {
    public HealingValidationResult {
        Objects.requireNonNull(recipe, "recipe");
        disabledReason = disabledReason == null ? Optional.empty() : disabledReason;
        missingResources = missingResources == null ? List.of() : List.copyOf(missingResources);
        missingFacilityRequirement = missingFacilityRequirement == null ? Optional.empty() : missingFacilityRequirement;
        requiredGemType = requiredGemType == null ? Optional.empty() : requiredGemType;
        estimatedHealingTime = estimatedHealingTime == null ? Duration.ZERO : estimatedHealingTime;
        requiredResources = requiredResources == null ? Map.of() : Map.copyOf(requiredResources);
    }
}
