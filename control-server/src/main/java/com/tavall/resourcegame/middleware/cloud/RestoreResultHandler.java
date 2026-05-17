package com.tavall.resourcegame.middleware.cloud;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;
import java.util.UUID;

public final class RestoreResultHandler implements IRestoreResultHandler, ICloudControlDomain {
    public boolean record(CloudCommandResult result) {
        Optional<CloudCommand> command = getCloudRepository().findCommand(result.commandId());
        if (command.isEmpty() || command.orElseThrow().commandType() != CloudCommandType.RESTORE_BACKUP) {
            return false;
        }
        UUID restoreId = restoreIdFrom(command.orElseThrow().payloadJson());
        Optional<RestoreJob> restoreJob = getCloudRepository().findRestoreJob(restoreId);
        if (restoreJob.isEmpty()) {
            return false;
        }
        getCloudRepository().saveRestoreJob(restoreJob.orElseThrow().withResult(
                result.success() ? RestoreStatus.COMPLETED : RestoreStatus.FAILED,
                Optional.of(result.startedAt()),
                Optional.of(result.completedAt())
        ));
        return true;
    }

    private UUID restoreIdFrom(String payloadJson) {
        try {
            JsonNode payload = getCloudControlObjectMapper().readTree(payloadJson);
            return UUID.fromString(payload.get("restoreId").asText());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Restore command payload must include restoreId.", ex);
        }
    }
}
