package org.tavall.control.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Aggregated metadata for troop combat capabilities.
 */
public final class TroopMetaData {
    private final double combatMedian;
    private final double disciplineMedian;
    private final double moraleMedian;
    private final Map<Integer, Integer> tierCounts;

    public TroopMetaData(
            @JsonProperty("combatMedian") double combatMedian,
            @JsonProperty("disciplineMedian") double disciplineMedian,
            @JsonProperty("moraleMedian") double moraleMedian
    ) {
        this(combatMedian, disciplineMedian, moraleMedian, Map.of());
    }

    @JsonCreator
    public TroopMetaData(
            @JsonProperty("combatMedian") double combatMedian,
            @JsonProperty("disciplineMedian") double disciplineMedian,
            @JsonProperty("moraleMedian") double moraleMedian,
            @JsonProperty("tierCounts") Map<Integer, Integer> tierCounts
    ) {
        this.combatMedian = combatMedian;
        this.disciplineMedian = disciplineMedian;
        this.moraleMedian = moraleMedian;
        this.tierCounts = tierCounts == null ? Map.of() : Map.copyOf(tierCounts);
    }

    public double combatMedian() {
        return combatMedian;
    }

    public double disciplineMedian() {
        return disciplineMedian;
    }

    public double moraleMedian() {
        return moraleMedian;
    }

    public Map<Integer, Integer> tierCounts() {
        return tierCounts;
    }

    public TroopMetaData withTierCounts(Map<Integer, Integer> tierCounts) {
        return new TroopMetaData(combatMedian, disciplineMedian, moraleMedian, tierCounts);
    }

    /**
     * Returns current aggregate military power.
     */
    public int estimatedMight(int troopCount) {
        if (tierCounts.isEmpty()) {
            return Math.max(0, troopCount);
        }
        return tierCounts.entrySet().stream()
                .mapToInt(entry -> {
                    int tier = entry.getKey() == null ? 0 : entry.getKey();
                    int count = entry.getValue() == null ? 0 : entry.getValue();
                    if (tier < 1 || count <= 0) {
                        return 0;
                    }
                    return tier * count;
                })
                .sum();
    }

    public static TroopMetaData defaults() {
        return new TroopMetaData(0.3, 0.3, 0.6, Map.of());
    }
}
