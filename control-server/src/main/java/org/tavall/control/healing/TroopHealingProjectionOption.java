package org.tavall.control.healing;

import org.tavall.control.asset.GlobalAssetId;
import org.tavall.control.projection.PlatformInteractionType;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record TroopHealingProjectionOption(
        String actionId,
        String recipeId,
        HealingMode healingMode,
        boolean enabled,
        Optional<String> disabledReason,
        Map<GlobalAssetId, Integer> requiredResources,
        List<GlobalAssetId> missingResources,
        Optional<String> missingFacilityRequirement,
        Optional<GemType> requiredGemType,
        Duration estimatedHealingTime,
        List<GlobalAssetId> globalAssetIds,
        Map<GlobalAssetId, String> platformAssetReferences,
        PlatformInteractionType interactionType
) {
    public TroopHealingProjectionOption {
        if (actionId == null || actionId.isBlank()) {
            throw new IllegalArgumentException("actionId is required.");
        }
        if (recipeId == null || recipeId.isBlank()) {
            throw new IllegalArgumentException("recipeId is required.");
        }
        disabledReason = disabledReason == null ? Optional.empty() : disabledReason;
        requiredResources = requiredResources == null ? Map.of() : Map.copyOf(requiredResources);
        missingResources = missingResources == null ? List.of() : List.copyOf(missingResources);
        missingFacilityRequirement = missingFacilityRequirement == null ? Optional.empty() : missingFacilityRequirement;
        requiredGemType = requiredGemType == null ? Optional.empty() : requiredGemType;
        estimatedHealingTime = estimatedHealingTime == null ? Duration.ZERO : estimatedHealingTime;
        globalAssetIds = globalAssetIds == null ? List.of() : List.copyOf(globalAssetIds);
        platformAssetReferences = platformAssetReferences == null ? Map.of() : Map.copyOf(platformAssetReferences);
        if (interactionType == null) {
            throw new IllegalArgumentException("interactionType is required.");
        }
    }
}
