package org.tavall.control.cloud;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.Optional;

public final class AgentCommandExecutionHandler implements IAgentCommandExecutionHandler, CloudAgentDomain {
    public CloudCommandResult execute(CloudCommand command, Instant now) {
        if (command.commandType() == null) {
            return new CloudCommandResult(command.commandId(), command.nodeId(), false, Optional.empty(), "Command type is required.",
                    now, now, "", "command type is required", java.util.Map.of());
        }
        if (command.expiresAt().isPresent() && now.isAfter(command.expiresAt().get())) {
            return new CloudCommandResult(command.commandId(), command.nodeId(), false, Optional.empty(), "Command expired.",
                    now, now, "", "expired", java.util.Map.of());
        }
        if (!getCloudCommandSignatureHandler().verify(command)) {
            return new CloudCommandResult(command.commandId(), command.nodeId(), false, Optional.empty(), "Command signature rejected.",
                    now, now, "", "invalid signature", java.util.Map.of("rejected", "true", "reason", "invalid-signature"));
        }
        return switch (command.commandType()) {
            case INSTALL_WORKLOAD, START_WORKLOAD, STOP_WORKLOAD, RESTART_WORKLOAD, DELETE_WORKLOAD -> executeWorkloadCommand(command, now);
            case RUN_HEALTH_CHECK, COLLECT_LOGS, RUN_BACKUP, OPEN_PORT, CLOSE_PORT, APPLY_FIREWALL_RULES, APPLY_PROXY_CONFIG,
                    RESTORE_BACKUP, UPDATE_AGENT, DRAIN_NODE, CANCEL_COMMAND -> new CloudCommandResult(command.commandId(), command.nodeId(), true,
                    Optional.of(0), "Accepted typed command " + command.commandType() + ".", now, now, "accepted", "",
                    java.util.Map.of());
        };
    }

    private CloudCommandResult executeWorkloadCommand(CloudCommand command, Instant now) {
        Optional<String> runtime = runtimeFrom(command);
        if (runtime.isEmpty()) {
            return rejected(command, now, "Workload command payload must declare runtime: systemd, tmux, or raw_process.");
        }
        return switch (runtime.orElseThrow()) {
            case "systemd" -> getSystemdWorkloadRuntimeAdapter().execute(command, now);
            case "tmux" -> getTmuxWorkloadRuntimeAdapter().execute(command, now);
            case "raw_process" -> getRawProcessWorkloadRuntimeAdapter().execute(command, now);
            default -> rejected(command, now, "Unsupported workload runtime adapter: " + runtime.orElseThrow() + ".");
        };
    }

    private Optional<String> runtimeFrom(CloudCommand command) {
        try {
            JsonNode payload = getCloudAgentObjectMapper().readTree(command.payloadJson());
            JsonNode runtime = payload.get("runtime");
            if (runtime == null || !runtime.isTextual()) {
                return Optional.empty();
            }
            return Optional.of(runtime.asText().trim().toLowerCase());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private CloudCommandResult rejected(CloudCommand command, Instant now, String reason) {
        return new CloudCommandResult(command.commandId(), command.nodeId(), false, Optional.empty(), reason,
                now, now, "", reason, java.util.Map.of("rejected", "true"));
    }
}
