package com.tavall.resourcegame.middleware.cloud;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public final class SystemdWorkloadRuntimeAdapter implements ISystemdWorkloadRuntimeAdapter, ICloudAgentDomain {
    /**
     * First-pass agent boundary: validate typed systemd work without exposing arbitrary shell execution.
     */
    public CloudCommandResult execute(CloudCommand command, Instant now) {
        JsonNode payload = payload(command);
        Optional<String> validationError = validate(payload, "unitName");
        if (validationError.isPresent()) {
            return rejected(command, now, validationError.orElseThrow());
        }
        return new CloudCommandResult(command.commandId(), command.nodeId(), true, Optional.of(0),
                "Accepted systemd runtime command " + command.commandType() + ".", now, now,
                "systemd adapter accepted unit=" + payload.get("unitName").asText(), "", Map.of(
                "runtime", "systemd",
                "workloadId", payload.get("workloadId").asText(),
                "unitName", payload.get("unitName").asText()
        ));
    }

    private JsonNode payload(CloudCommand command) {
        try {
            return getCloudAgentObjectMapper().readTree(command.payloadJson());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JSON payload.");
        }
    }

    private Optional<String> validate(JsonNode payload, String targetField) {
        if (payload.has("shellCommand") || payload.has("commandLine")) {
            return Optional.of("Systemd adapter rejects raw shell payloads.");
        }
        if (!payload.hasNonNull("workloadId") || !payload.get("workloadId").isTextual() || payload.get("workloadId").asText().isBlank()) {
            return Optional.of("Systemd adapter requires workloadId.");
        }
        if (!payload.hasNonNull(targetField) || !payload.get(targetField).isTextual() || payload.get(targetField).asText().isBlank()) {
            return Optional.of("Systemd adapter requires " + targetField + ".");
        }
        return Optional.empty();
    }

    private CloudCommandResult rejected(CloudCommand command, Instant now, String reason) {
        return new CloudCommandResult(command.commandId(), command.nodeId(), false, Optional.empty(),
                reason, now, now, "", reason, Map.of("rejected", "true", "runtime", "systemd"));
    }
}
