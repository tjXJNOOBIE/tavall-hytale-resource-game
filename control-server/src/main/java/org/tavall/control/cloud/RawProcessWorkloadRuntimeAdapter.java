package org.tavall.control.cloud;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public final class RawProcessWorkloadRuntimeAdapter implements IRawProcessWorkloadRuntimeAdapter, CloudAgentDomain {
    /**
     * Raw process support is restricted to typed dev/test workload commands, never arbitrary shell text.
     */
    public CloudCommandResult execute(CloudCommand command, Instant now) {
        JsonNode payload = payload(command);
        Optional<String> validationError = validate(payload);
        if (validationError.isPresent()) {
            return rejected(command, now, validationError.orElseThrow());
        }
        return new CloudCommandResult(command.commandId(), command.nodeId(), true, Optional.of(0),
                "Accepted raw-process runtime command " + command.commandType() + ".", now, now,
                "raw-process adapter accepted process=" + payload.get("processKey").asText(), "", Map.of(
                "runtime", "raw_process",
                "workloadId", payload.get("workloadId").asText(),
                "processKey", payload.get("processKey").asText()
        ));
    }

    private JsonNode payload(CloudCommand command) {
        try {
            return getCloudAgentObjectMapper().readTree(command.payloadJson());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JSON payload.");
        }
    }

    private Optional<String> validate(JsonNode payload) {
        if (payload.has("shellCommand") || payload.has("commandLine")) {
            return Optional.of("Raw-process adapter rejects raw shell payloads.");
        }
        if (!payload.hasNonNull("workloadId") || !payload.get("workloadId").isTextual() || payload.get("workloadId").asText().isBlank()) {
            return Optional.of("Raw-process adapter requires workloadId.");
        }
        if (!payload.hasNonNull("processKey") || !payload.get("processKey").isTextual() || payload.get("processKey").asText().isBlank()) {
            return Optional.of("Raw-process adapter requires processKey.");
        }
        return Optional.empty();
    }

    private CloudCommandResult rejected(CloudCommand command, Instant now, String reason) {
        return new CloudCommandResult(command.commandId(), command.nodeId(), false, Optional.empty(),
                reason, now, now, "", reason, Map.of("rejected", "true", "runtime", "raw_process"));
    }
}
