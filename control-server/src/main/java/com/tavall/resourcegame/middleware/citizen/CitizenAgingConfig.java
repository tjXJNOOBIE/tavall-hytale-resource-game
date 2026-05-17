package com.tavall.resourcegame.middleware.citizen;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

public record CitizenAgingConfig(
        boolean enabled,
        long realMillisPerGameDay,
        int gameDaysPerYear,
        Map<CitizenAgeStage, Integer> ageStageThresholds,
        long maintenanceIntervalMillis,
        Map<String, String> metadata
) implements IDependencyInjectableConcrete {
    public CitizenAgingConfig {
        if (realMillisPerGameDay <= 0) {
            throw new IllegalArgumentException("realMillisPerGameDay must be positive.");
        }
        if (gameDaysPerYear <= 0) {
            throw new IllegalArgumentException("gameDaysPerYear must be positive.");
        }
        EnumMap<CitizenAgeStage, Integer> thresholds = new EnumMap<>(CitizenAgeStage.class);
        thresholds.putAll(defaultThresholds());
        if (ageStageThresholds != null) {
            thresholds.putAll(ageStageThresholds);
        }
        ageStageThresholds = Map.copyOf(thresholds);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static CitizenAgingConfig defaults() {
        return new CitizenAgingConfig(true, Duration.ofDays(1).toMillis(), 12, defaultThresholds(), Duration.ofMinutes(15).toMillis(), Map.of(
                "prototypeBalance", "1 real day = 1 in-game month; 12 real days = 1 citizen year"
        ));
    }

    public static Map<CitizenAgeStage, Integer> defaultThresholds() {
        EnumMap<CitizenAgeStage, Integer> thresholds = new EnumMap<>(CitizenAgeStage.class);
        thresholds.put(CitizenAgeStage.INFANT, 0);
        thresholds.put(CitizenAgeStage.CHILD, 2);
        thresholds.put(CitizenAgeStage.TEEN, 12);
        thresholds.put(CitizenAgeStage.YOUNG_ADULT, 18);
        thresholds.put(CitizenAgeStage.ADULT, 30);
        thresholds.put(CitizenAgeStage.MIDDLE_AGED, 50);
        thresholds.put(CitizenAgeStage.ELDER, 65);
        thresholds.put(CitizenAgeStage.DECEASED, Integer.MAX_VALUE);
        return thresholds;
    }
}
