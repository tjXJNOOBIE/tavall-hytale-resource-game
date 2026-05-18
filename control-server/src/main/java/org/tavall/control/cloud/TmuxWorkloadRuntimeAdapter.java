package org.tavall.control.cloud;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public final class TmuxWorkloadRuntimeAdapter implements ITmuxWorkloadRuntimeAdapter, CloudAgentDomain {
    /**
     * Tmux remains a controlled game-server adapter boundary; only typed sessions are accepted.
     */
    public CloudCommandResult execute(CloudCommand command, Instant now) {
        JsonNode payload = payload(command);
        Optional<String> validationError = validate(payload);
        if (validationError.isPresent()) {
            return rejected(command, now, validationError.orElseThrow());
        }
        return new CloudCommandResult(command.commandId(), command.nodeId(), true, Optional.of(0),
                "Accepted tmux runtime command " + command.commandType() + ".", now, now,
                "tmux adapter accepted session=" + payload.get("sessionName").asText(), "", Map.of(
                "runtime", "tmux",
                "workloadId", payload.get("workloadId").asText(),
                "sessionName", payload.get("sessionName").asText()
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
            return Optional.of("Tmux adapter rejects raw shell payloads.");
        }
        if (!payload.hasNonNull("workloadId") || !payload.get("workloadId").isTextual() || payload.get("workloadId").asText().isBlank()) {
            return Optional.of("Tmux adapter requires workloadId.");
        }
        if (!payload.hasNonNull("sessionName") || !payload.get("sessionName").isTextual() || payload.get("sessionName").asText().isBlank()) {
            return Optional.of("Tmux adapter requires sessionName.");
        }
        return Optional.empty();
    }

    private CloudCommandResult rejected(CloudCommand command, Instant now, String reason) {
        return new CloudCommandResult(command.commandId(), command.nodeId(), false, Optional.empty(),
                reason, now, now, "", reason, Map.of("rejected", "true", "runtime", "tmux"));
    }
}
