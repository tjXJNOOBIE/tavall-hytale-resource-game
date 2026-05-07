package com.tavall.hytale.resourcegame.middleware.cloud;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record BackupPlan(
        UUID backupPlanId,
        Optional<UUID> workloadId,
        Optional<UUID> nodeId,
        BackupType backupType,
        String schedule,
        boolean enabled,
        String retentionPolicy,
        String destination,
        boolean verifyAfterBackup,
        Map<String, String> metadata
) {
    public BackupPlan {
        workloadId = workloadId == null ? Optional.empty() : workloadId;
        nodeId = nodeId == null ? Optional.empty() : nodeId;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
