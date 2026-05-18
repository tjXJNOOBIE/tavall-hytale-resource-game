package org.tavall.control.healing;

import org.tavall.control.asset.GlobalAssetId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class HealingFacilityDefinitionRegistry {
    private final List<HealingFacilityLevelDefinition> definitions;

    public HealingFacilityDefinitionRegistry() {
        this.definitions = List.copyOf(buildDefinitions());
    }

    public Optional<HealingFacilityLevelDefinition> definitionForLevel(int buildingLevel) {
        return definitions.stream()
                .filter(definition -> definition.buildingLevel() == buildingLevel)
                .findFirst();
    }

    public List<HealingFacilityLevelDefinition> definitions() {
        return definitions;
    }

    private List<HealingFacilityLevelDefinition> buildDefinitions() {
        ArrayList<HealingFacilityLevelDefinition> result = new ArrayList<>();
        for (int level = 1; level <= 30; level++) {
            result.add(definitionForBand(level));
        }
        return result;
    }

    private HealingFacilityLevelDefinition definitionForBand(int level) {
        if (level <= 3) {
            return create(level, HealingFacilityType.FIELD_TENT, 1, 2, Set.of(WoundType.GENERAL_WOUND, WoundType.EXHAUSTED), 1.0d + (level - 1) * 0.05d, 1.0d + (level - 1) * 0.08d, 1.0d - (level - 1) * 0.02d, 2 + level);
        }
        if (level <= 7) {
            return create(level, HealingFacilityType.INFIRMARY, 1, 4, Set.of(WoundType.GENERAL_WOUND, WoundType.EXHAUSTED), 1.12d + (level - 4) * 0.05d, 1.2d + (level - 4) * 0.08d, 0.93d - (level - 4) * 0.02d, 6 + level);
        }
        if (level <= 11) {
            return create(level, HealingFacilityType.HERBALIST_HUT, 1, 5, Set.of(WoundType.GENERAL_WOUND, WoundType.EXHAUSTED), 1.32d + (level - 8) * 0.05d, 1.45d + (level - 8) * 0.08d, 0.84d - (level - 8) * 0.02d, 10 + level);
        }
        if (level <= 15) {
            return create(level, HealingFacilityType.APOTHECARY, 1, 6, Set.of(WoundType.GENERAL_WOUND, WoundType.EXHAUSTED, WoundType.POISONED), 1.5d + (level - 12) * 0.05d, 1.75d + (level - 12) * 0.08d, 0.76d - (level - 12) * 0.02d, 14 + level);
        }
        if (level <= 19) {
            return create(level, HealingFacilityType.FIELD_HOSPITAL, 1, 8, Set.of(WoundType.GENERAL_WOUND, WoundType.EXHAUSTED, WoundType.POISONED), 1.7d + (level - 16) * 0.05d, 2.1d + (level - 16) * 0.1d, 0.68d - (level - 16) * 0.02d, 20 + level);
        }
        if (level <= 23) {
            return create(level, HealingFacilityType.SURGICAL_HALL, 1, 9, Set.of(WoundType.GENERAL_WOUND, WoundType.EXHAUSTED, WoundType.POISONED), 1.88d + (level - 20) * 0.06d, 2.5d + (level - 20) * 0.12d, 0.6d - (level - 20) * 0.02d, 28 + level);
        }
        if (level <= 27) {
            return create(level, HealingFacilityType.SHRINE, 1, 10, Set.of(WoundType.GENERAL_WOUND, WoundType.EXHAUSTED, WoundType.POISONED, WoundType.MAGIC_WOUND), 2.1d + (level - 24) * 0.06d, 3.0d + (level - 24) * 0.14d, 0.52d - (level - 24) * 0.02d, 36 + level);
        }
        return create(level, HealingFacilityType.GUILD_HOSPITAL, 1, 10, Set.of(WoundType.GENERAL_WOUND, WoundType.EXHAUSTED, WoundType.POISONED, WoundType.MAGIC_WOUND), 2.4d + (level - 28) * 0.08d, 3.6d + (level - 28) * 0.18d, 0.44d - (level - 28) * 0.03d, 48 + level);
    }

    private HealingFacilityLevelDefinition create(
            int level,
            HealingFacilityType facilityType,
            int minTroopTier,
            int maxTroopTier,
            Set<WoundType> supportedWounds,
            double foodOnlySpeed,
            double properTreatmentSpeed,
            double resourceCostModifier,
            int capacity
    ) {
        return new HealingFacilityLevelDefinition(
                level,
                facilityType,
                facilityType.displayName(),
                minTroopTier,
                maxTroopTier,
                supportedWounds,
                foodOnlySpeed,
                properTreatmentSpeed,
                resourceCostModifier,
                capacity,
                new GlobalAssetId("building.healing." + facilityType.assetKey() + ".level_" + level)
        );
    }
}
