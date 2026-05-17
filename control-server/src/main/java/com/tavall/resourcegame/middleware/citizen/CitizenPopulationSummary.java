package com.tavall.resourcegame.middleware.citizen;

import com.tavall.resourcegame.domain.CitizenJobType;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

public record CitizenPopulationSummary(
        CitizenSummaryScope scope,
        int totalCitizens,
        int activeCitizens,
        int activeTroops,
        int inTraining,
        int wounded,
        int recovering,
        int retired,
        int dead,
        Map<CitizenAgeStage, Integer> ageStageCounts,
        Map<CitizenJobType, Integer> jobCounts,
        Map<CitizenMoraleState, Integer> moraleCounts,
        Map<CitizenHealthState, Integer> healthCounts,
        Map<CitizenHousingState, Integer> housingCounts,
        Map<CitizenNutritionState, Integer> nutritionCounts,
        Instant updatedAt
) {
    public CitizenPopulationSummary {
        ageStageCounts = copyEnumMap(CitizenAgeStage.class, ageStageCounts);
        jobCounts = copyEnumMap(CitizenJobType.class, jobCounts);
        moraleCounts = copyEnumMap(CitizenMoraleState.class, moraleCounts);
        healthCounts = copyEnumMap(CitizenHealthState.class, healthCounts);
        housingCounts = copyEnumMap(CitizenHousingState.class, housingCounts);
        nutritionCounts = copyEnumMap(CitizenNutritionState.class, nutritionCounts);
    }

    private static <E extends Enum<E>> Map<E, Integer> copyEnumMap(Class<E> enumClass, Map<E, Integer> source) {
        EnumMap<E, Integer> copy = new EnumMap<>(enumClass);
        if (source != null) {
            copy.putAll(source);
        }
        return Map.copyOf(copy);
    }
}
