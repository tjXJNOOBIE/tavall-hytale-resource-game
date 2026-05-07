package com.tavall.hytale.resourcegame.middleware.cloud;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class BackupJobCreationHandler {
    private final InMemoryCloudRepository repository;
    private final CloudCommandCreationHandler commandCreationHandler;

    public BackupJobCreationHandler(InMemoryCloudRepository repository, CloudCommandCreationHandler commandCreationHandler) {
        this.repository = repository;
        this.commandCreationHandler = commandCreationHandler;
    }

    public BackupJob createAndCommand(BackupPlan plan, String sourcePath, UUID requestedBy, java.time.Instant now) {
        BackupJob job = new BackupJob(UUID.randomUUID(), plan.workloadId(), plan.nodeId(), plan.backupType(), sourcePath,
                plan.destination(), BackupStatus.PENDING, Optional.empty(), Optional.empty(), Optional.empty(),
                BackupVerificationStatus.NOT_VERIFIED, Map.of("planId", plan.backupPlanId().toString()));
        repository.saveBackupPlan(plan);
        repository.saveBackupJob(job);
        plan.nodeId().ifPresent(nodeId -> commandCreationHandler.create(nodeId, CloudCommandType.RUN_BACKUP,
                "{\"backupId\":\"" + job.backupId() + "\"}", requestedBy, job.backupId(), now));
        return job;
    }
}
