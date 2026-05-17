package com.tavall.resourcegame.middleware.cloud;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record BackupJob(
        UUID backupId,
        Optional<UUID> workloadId,
        Optional<UUID> nodeId,
        BackupType backupType,
        String sourcePath,
        String destination,
        BackupStatus status,
        Optional<Instant> startedAt,
        Optional<Instant> completedAt,
        Optional<String> checksum,
        BackupVerificationStatus verificationStatus,
        Map<String, String> metadata
) {
    public BackupJob {
        workloadId = workloadId == null ? Optional.empty() : workloadId;
        nodeId = nodeId == null ? Optional.empty() : nodeId;
        startedAt = startedAt == null ? Optional.empty() : startedAt;
        completedAt = completedAt == null ? Optional.empty() : completedAt;
        checksum = checksum == null ? Optional.empty() : checksum;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public BackupJob withResult(BackupStatus nextStatus, Optional<Instant> nextStartedAt, Optional<Instant> nextCompletedAt,
                                Optional<String> nextChecksum, BackupVerificationStatus nextVerificationStatus) {
        return new BackupJob(backupId, workloadId, nodeId, backupType, sourcePath, destination, nextStatus,
                nextStartedAt, nextCompletedAt, nextChecksum, nextVerificationStatus, metadata);
    }

    public BackupJob withVerificationStatus(BackupVerificationStatus nextVerificationStatus) {
        return new BackupJob(backupId, workloadId, nodeId, backupType, sourcePath, destination, status,
                startedAt, completedAt, checksum, nextVerificationStatus, metadata);
    }
}
