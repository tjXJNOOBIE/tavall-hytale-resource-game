package com.tavall.hytale.resourcegame.middleware.cloud;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class BackupJobCreationHandler implements IBackupJobCreationHandler, ICloudControlDomain {
    public BackupJob createAndCommand(BackupPlan plan, String sourcePath, UUID requestedBy, java.time.Instant now) {
        BackupJob job = new BackupJob(UUID.randomUUID(), plan.workloadId(), plan.nodeId(), plan.backupType(), sourcePath,
                plan.destination(), BackupStatus.PENDING, Optional.empty(), Optional.empty(), Optional.empty(),
                BackupVerificationStatus.NOT_VERIFIED, Map.of("planId", plan.backupPlanId().toString()));
        getCloudRepository().saveBackupPlan(plan);
        getCloudRepository().saveBackupJob(job);
        plan.nodeId().ifPresent(nodeId -> getCloudCommandCreationHandler().create(nodeId, CloudCommandType.RUN_BACKUP,
                "{\"backupId\":\"" + job.backupId() + "\"}", requestedBy, job.backupId(), now));
        return job;
    }
}
