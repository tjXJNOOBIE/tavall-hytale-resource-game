package com.tavall.hytale.resourcegame.middleware.clock;

import com.tavall.hytale.resourcegame.middleware.common.MetadataMaps;

import java.util.List;
import java.util.Map;

public record KingdomClockProjection(
        String kingdomId,
        long currentKingdomDay,
        int currentHour,
        int currentMinute,
        KingdomTimePhase currentPhase,
        boolean isDay,
        boolean isNight,
        KingdomClockMode mode,
        String timezoneId,
        List<KingdomScheduleRule> activeScheduleRules,
        KingdomVisualMood visualMood,
        Map<String, String> metadata
) {
    public KingdomClockProjection {
        activeScheduleRules = activeScheduleRules == null ? List.of() : List.copyOf(activeScheduleRules);
        metadata = MetadataMaps.immutable(metadata);
    }
}
