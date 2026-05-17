package com.tavall.resourcegame.middleware.healing;

import com.tavall.resourcegame.middleware.asset.GlobalAssetId;

import java.util.Objects;
import java.util.Set;

public record HealingFacilityLevelDefinition(
        int buildingLevel,
        HealingFacilityType facilityType,
        String facilityName,
        int minTroopTierSupported,
        int maxTroopTierSupported,
        Set<WoundType> supportedProperTreatmentWoundTypes,
        double foodOnlyHealingSpeedModifier,
        double properTreatmentSpeedModifier,
        double resourceCostModifier,
        int troopHealingCapacity,
        GlobalAssetId globalAssetId
) {
    public HealingFacilityLevelDefinition {
        if (buildingLevel < 1 || buildingLevel > 30) {
            throw new HealingValidationException("Healing facility level must be 1-30.");
        }
        Objects.requireNonNull(facilityType, "facilityType");
        facilityName = facilityName == null || facilityName.isBlank() ? facilityType.displayName() : facilityName;
        minTroopTierSupported = Math.max(1, minTroopTierSupported);
        maxTroopTierSupported = Math.max(minTroopTierSupported, Math.min(10, maxTroopTierSupported));
        supportedProperTreatmentWoundTypes = supportedProperTreatmentWoundTypes == null ? Set.of() : Set.copyOf(supportedProperTreatmentWoundTypes);
        foodOnlyHealingSpeedModifier = Math.max(1.0d, foodOnlyHealingSpeedModifier);
        properTreatmentSpeedModifier = Math.max(1.0d, properTreatmentSpeedModifier);
        resourceCostModifier = Math.max(0.2d, Math.min(1.5d, resourceCostModifier));
        troopHealingCapacity = Math.max(1, troopHealingCapacity);
        Objects.requireNonNull(globalAssetId, "globalAssetId");
    }

    public boolean supportsProperTreatment(WoundType woundType, int troopTier) {
        return supportedProperTreatmentWoundTypes.contains(woundType)
                && troopTier >= minTroopTierSupported
                && troopTier <= maxTroopTierSupported;
    }
}
