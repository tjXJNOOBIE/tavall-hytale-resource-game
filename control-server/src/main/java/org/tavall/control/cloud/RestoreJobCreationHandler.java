package org.tavall.control.cloud;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class RestoreJobCreationHandler implements IRestoreJobCreationHandler, CloudControlDomain {
    /**
     * Restore is intentionally gated by backup verification and the high-risk RESTORE_BACKUP command policy.
     */
    public RestoreJob createAndCommand(UUID backupId, Optional<UUID> targetWorkloadId, Optional<UUID> targetNodeId,
                                       UUID requestedBy, boolean approvalPresent, Instant now) {
        BackupJob backupJob = getCloudRepository().findBackupJob(backupId).orElseThrow();
        if (backupJob.status() != BackupStatus.COMPLETED || backupJob.verificationStatus() != BackupVerificationStatus.VERIFIED) {
            throw new IllegalStateException("Only completed and verified backups can be restored.");
        }
        UUID nodeId = targetNodeId.or(() -> backupJob.nodeId())
                .orElseThrow(() -> new IllegalArgumentException("Restore target node is required."));
        RestoreJob restoreJob = new RestoreJob(UUID.randomUUID(), backupId, targetWorkloadId, Optional.of(nodeId),
                RestoreStatus.PENDING, Optional.empty(), Optional.empty(), Map.of());
        getCloudCommandCreationHandler().create(nodeId, CloudCommandType.RESTORE_BACKUP,
                restorePayload(restoreJob, approvalPresent), requestedBy, restoreJob.restoreId(), now);
        getCloudRepository().saveRestoreJob(restoreJob);
        return restoreJob;
    }

    private String restorePayload(RestoreJob restoreJob, boolean approvalPresent) {
        ObjectNode payload = getCloudControlObjectMapper().createObjectNode();
        payload.put("backupId", restoreJob.backupId().toString());
        payload.put("restoreId", restoreJob.restoreId().toString());
        payload.put("approvalPresent", approvalPresent);
        payload.put("reason", "restore verified backup");
        restoreJob.targetWorkloadId().ifPresent(value -> payload.put("workloadId", value.toString()));
        return payload.toString();
    }
}
