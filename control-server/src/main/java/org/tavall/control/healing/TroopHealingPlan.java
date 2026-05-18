package org.tavall.control.healing;

import org.tavall.control.asset.GlobalAssetId;
import org.tavall.control.common.MetadataMaps;
import org.tavall.control.troop.TroopId;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record TroopHealingPlan(
        UUID healingPlanId,
        TroopId troopId,
        UUID woundId,
        HealingMode healingMode,
        Optional<String> selectedRecipeId,
        Optional<String> facilityId,
        Instant startedAt,
        Instant completesAt,
        HealingState state,
        Map<GlobalAssetId, Integer> requiredResources,
        Map<GlobalAssetId, Integer> consumedResources,
        Map<String, String> metadata
) {
    public TroopHealingPlan {
        Objects.requireNonNull(healingPlanId, "healingPlanId");
        Objects.requireNonNull(troopId, "troopId");
        Objects.requireNonNull(woundId, "woundId");
        Objects.requireNonNull(healingMode, "healingMode");
        selectedRecipeId = selectedRecipeId == null ? Optional.empty() : selectedRecipeId;
        facilityId = facilityId == null ? Optional.empty() : facilityId;
        Objects.requireNonNull(startedAt, "startedAt");
        Objects.requireNonNull(completesAt, "completesAt");
        state = state == null ? HealingState.PENDING : state;
        requiredResources = immutableAmounts(requiredResources);
        consumedResources = immutableAmounts(consumedResources);
        metadata = MetadataMaps.immutable(metadata);
    }

    public static TroopHealingPlan active(
            TroopId troopId,
            TroopWound wound,
            TroopHealingRecipe recipe,
            Optional<HealingFacilityLevelDefinition> facility,
            Instant startedAt,
            Instant completesAt,
            Map<GlobalAssetId, Integer> requiredResources
    ) {
        Optional<String> facilityId = facility.map(definition -> "healing-facility-level-" + definition.buildingLevel());
        return new TroopHealingPlan(
                UUID.randomUUID(),
                troopId,
                wound.woundId(),
                recipe.healingMode(),
                Optional.of(recipe.recipeId()),
                facilityId,
                startedAt,
                completesAt,
                HealingState.ACTIVE,
                requiredResources,
                requiredResources,
                Map.of("woundType", wound.woundType().name(), "severity", wound.severity().name())
        );
    }

    public TroopHealingPlan withState(HealingState state) {
        return new TroopHealingPlan(healingPlanId, troopId, woundId, healingMode, selectedRecipeId, facilityId, startedAt, completesAt, state, requiredResources, consumedResources, metadata);
    }

    private static Map<GlobalAssetId, Integer> immutableAmounts(Map<GlobalAssetId, Integer> amounts) {
        LinkedHashMap<GlobalAssetId, Integer> copy = new LinkedHashMap<>();
        if (amounts != null) {
            for (Map.Entry<GlobalAssetId, Integer> entry : amounts.entrySet()) {
                copy.put(entry.getKey(), Math.max(0, entry.getValue() == null ? 0 : entry.getValue()));
            }
        }
        return Map.copyOf(copy);
    }
}
