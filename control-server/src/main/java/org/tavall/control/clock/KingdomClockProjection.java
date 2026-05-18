package org.tavall.control.clock;

import org.tavall.control.common.MetadataMaps;

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
