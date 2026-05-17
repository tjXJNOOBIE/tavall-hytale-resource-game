package com.tavall.resourcegame.middleware.clock;

import com.tavall.resourcegame.middleware.common.MetadataMaps;

import java.util.List;
import java.util.Map;

public record KingdomScheduledStateChangeResult(
        String kingdomId,
        long evaluatedAtEpochMinute,
        List<KingdomScheduleRule> activeRules,
        List<String> appliedEffects,
        Map<String, String> metadata
) {
    public KingdomScheduledStateChangeResult {
        activeRules = activeRules == null ? List.of() : List.copyOf(activeRules);
        appliedEffects = appliedEffects == null ? List.of() : List.copyOf(appliedEffects);
        metadata = MetadataMaps.immutable(metadata);
    }
}
