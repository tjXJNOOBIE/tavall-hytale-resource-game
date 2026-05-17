package com.tavall.resourcegame.middleware.cloud;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;
import java.util.UUID;

public final class BackupResultHandler implements IBackupResultHandler, ICloudControlDomain {
    public boolean record(CloudCommandResult result) {
        Optional<CloudCommand> command = getCloudRepository().findCommand(result.commandId());
        if (command.isEmpty() || command.orElseThrow().commandType() != CloudCommandType.RUN_BACKUP) {
            return false;
        }
        UUID backupId = backupIdFrom(command.orElseThrow().payloadJson());
        Optional<BackupJob> backupJob = getCloudRepository().findBackupJob(backupId);
        if (backupJob.isEmpty()) {
            return false;
        }
        BackupStatus status = result.success() ? BackupStatus.COMPLETED : BackupStatus.FAILED;
        Optional<String> checksum = Optional.ofNullable(result.metadata().get("checksum")).filter(value -> !value.isBlank());
        getCloudRepository().saveBackupJob(backupJob.orElseThrow().withResult(
                status,
                Optional.of(result.startedAt()),
                Optional.of(result.completedAt()),
                checksum,
                BackupVerificationStatus.NOT_VERIFIED
        ));
        return true;
    }

    private UUID backupIdFrom(String payloadJson) {
        try {
            JsonNode payload = getCloudControlObjectMapper().readTree(payloadJson);
            return UUID.fromString(payload.get("backupId").asText());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Backup command payload must include backupId.", ex);
        }
    }
}
