package org.tavall.control.clock;

import org.tavall.control.common.MetadataMaps;

import java.time.Instant;
import java.util.Map;

public record InteriorScheduleState(
        String interiorId,
        String kingdomId,
        boolean lightsEnabled,
        String occupancyHint,
        KingdomVisualMood activeMood,
        Instant updatedAt,
        Map<String, String> metadata
) {
    public InteriorScheduleState {
        occupancyHint = occupancyHint == null || occupancyHint.isBlank() ? "UNKNOWN" : occupancyHint;
        metadata = MetadataMaps.immutable(metadata);
    }
}
