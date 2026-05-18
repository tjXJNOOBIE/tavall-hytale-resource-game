package org.tavall.control.cloud;

import com.fasterxml.jackson.databind.JsonNode;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.io.IOException;
import java.util.UUID;

public final class CloudCommandValidationHandler implements ICloudCommandValidationHandler, ICloudControlDomain, IDependencyInjectableConcrete {
    public void validate(CloudCommandType commandType, String payloadJson) {
        if (commandType == null) {
            throw new IllegalArgumentException("Cloud command type is required.");
        }
        if (payloadJson == null || payloadJson.isBlank()) {
            throw new IllegalArgumentException("Cloud command payload JSON is required.");
        }
        JsonNode payload = parsePayload(payloadJson);
        if (!payload.isObject()) {
            throw new IllegalArgumentException("Cloud command payload must be a JSON object.");
        }
        validateTypedPayload(commandType, payload);
    }

    private JsonNode parsePayload(String payloadJson) {
        try {
            return getCloudControlObjectMapper().readTree(payloadJson);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Cloud command payload must be valid JSON.", exception);
        }
    }

    private void validateTypedPayload(CloudCommandType commandType, JsonNode payload) {
        switch (commandType) {
            case INSTALL_WORKLOAD, START_WORKLOAD, STOP_WORKLOAD, RESTART_WORKLOAD, DELETE_WORKLOAD ->
                    requireUuid(payload, "workloadId", commandType);
            case RUN_BACKUP, RESTORE_BACKUP -> requireUuid(payload, "backupId", commandType);
            default -> {
            }
        }
    }

    private void requireUuid(JsonNode payload, String fieldName, CloudCommandType commandType) {
        JsonNode field = payload.get(fieldName);
        if (field == null || field.isNull() || field.asText("").isBlank()) {
            throw new IllegalArgumentException(commandType + " payload requires " + fieldName + ".");
        }
        try {
            UUID.fromString(field.asText());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(commandType + " payload requires " + fieldName + " to be a UUID.", exception);
        }
    }
}
