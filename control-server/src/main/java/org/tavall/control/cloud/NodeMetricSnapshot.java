package org.tavall.control.cloud;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record NodeMetricSnapshot(
        UUID nodeId,
        double cpuUsagePercent,
        int ramUsedMb,
        int ramAvailableMb,
        int diskUsedGb,
        int diskAvailableGb,
        long networkRxBytes,
        long networkTxBytes,
        Instant collectedAt,
        Map<String, String> metadata
) {
    public NodeMetricSnapshot {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public double ramUsagePercent() {
        int total = ramUsedMb + ramAvailableMb;
        if (total <= 0) {
            return 0.0D;
        }
        return (ramUsedMb * 100.0D) / total;
    }

    public double diskUsagePercent() {
        int total = diskUsedGb + diskAvailableGb;
        if (total <= 0) {
            return 0.0D;
        }
        return (diskUsedGb * 100.0D) / total;
    }
}
