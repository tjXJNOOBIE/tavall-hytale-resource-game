package org.tavall.control.cloud;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record RestoreJob(
        UUID restoreId,
        UUID backupId,
        Optional<UUID> targetWorkloadId,
        Optional<UUID> targetNodeId,
        RestoreStatus status,
        Optional<Instant> startedAt,
        Optional<Instant> completedAt,
        Map<String, String> metadata
) {
    public RestoreJob {
        targetWorkloadId = targetWorkloadId == null ? Optional.empty() : targetWorkloadId;
        targetNodeId = targetNodeId == null ? Optional.empty() : targetNodeId;
        startedAt = startedAt == null ? Optional.empty() : startedAt;
        completedAt = completedAt == null ? Optional.empty() : completedAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public RestoreJob withResult(RestoreStatus nextStatus, Optional<Instant> nextStartedAt, Optional<Instant> nextCompletedAt) {
        return new RestoreJob(restoreId, backupId, targetWorkloadId, targetNodeId, nextStatus, nextStartedAt,
                nextCompletedAt, metadata);
    }
}
