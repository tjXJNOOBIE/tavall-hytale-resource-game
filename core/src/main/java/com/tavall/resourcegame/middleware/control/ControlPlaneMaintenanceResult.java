package com.tavall.resourcegame.middleware.control;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ControlPlaneMaintenanceResult(
        Instant evaluatedAt,
        int scheduledCommandsDispatched,
        int kingdomClocksTicked,
        int agingTicksCompleted,
        int kingdomScalingEvaluations,
        int scheduledStateChangesApplied,
        int fanoutRetriesMarkedForReview,
        List<ControlCommandResult> scheduledCommandResults,
        Map<String, String> metadata
) {
    public ControlPlaneMaintenanceResult {
        scheduledCommandResults = scheduledCommandResults == null ? List.of() : List.copyOf(scheduledCommandResults);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
