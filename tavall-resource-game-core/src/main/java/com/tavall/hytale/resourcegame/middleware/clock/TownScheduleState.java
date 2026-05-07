package com.tavall.hytale.resourcegame.middleware.clock;

import com.tavall.hytale.resourcegame.middleware.common.MetadataMaps;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record TownScheduleState(
        String townId,
        String kingdomId,
        boolean shopsOpen,
        KingdomVisualMood currentMood,
        List<KingdomScheduleWindow> activeScheduleWindows,
        Instant updatedAt,
        Map<String, String> metadata
) {
    public TownScheduleState {
        activeScheduleWindows = activeScheduleWindows == null ? List.of() : List.copyOf(activeScheduleWindows);
        metadata = MetadataMaps.immutable(metadata);
    }
}
