package com.tavall.resourcegame.middleware.cloud;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record WorkloadMetricSnapshot(
        UUID workloadId,
        UUID nodeId,
        Optional<Double> cpuUsagePercent,
        Optional<Integer> ramUsedMb,
        int restartCount,
        WorkloadHealthStatus healthStatus,
        Instant collectedAt,
        Map<String, String> metadata
) {
    public WorkloadMetricSnapshot {
        cpuUsagePercent = cpuUsagePercent == null ? Optional.empty() : cpuUsagePercent;
        ramUsedMb = ramUsedMb == null ? Optional.empty() : ramUsedMb;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
