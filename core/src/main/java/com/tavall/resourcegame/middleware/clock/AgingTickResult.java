package com.tavall.resourcegame.middleware.clock;

import com.tavall.resourcegame.middleware.common.MetadataMaps;

import java.util.List;
import java.util.Map;

public record AgingTickResult(
        String kingdomId,
        long evaluatedAtEpochMinute,
        int affectedCitizenCount,
        int affectedCompanionCount,
        List<String> eventsEmitted,
        Map<String, String> metadata
) {
    public AgingTickResult {
        eventsEmitted = eventsEmitted == null ? List.of() : List.copyOf(eventsEmitted);
        metadata = MetadataMaps.immutable(metadata);
    }
}
