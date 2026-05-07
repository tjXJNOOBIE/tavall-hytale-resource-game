package com.tavall.hytale.resourcegame.middleware.cloud;

import java.time.Instant;
import java.util.Optional;

public final class AgentCommandExecutionHandler {
    public CloudCommandResult execute(CloudCommand command, Instant now) {
        if (command.expiresAt().isPresent() && now.isAfter(command.expiresAt().get())) {
            return new CloudCommandResult(command.commandId(), command.nodeId(), false, Optional.empty(), "Command expired.",
                    now, now, "", "expired", java.util.Map.of());
        }
        return switch (command.commandType()) {
            case START_WORKLOAD, STOP_WORKLOAD, RESTART_WORKLOAD, RUN_HEALTH_CHECK, COLLECT_LOGS, RUN_BACKUP, OPEN_PORT, CLOSE_PORT,
                    APPLY_FIREWALL_RULES, APPLY_PROXY_CONFIG, INSTALL_WORKLOAD, DELETE_WORKLOAD, RESTORE_BACKUP, UPDATE_AGENT,
                    DRAIN_NODE, CANCEL_COMMAND -> new CloudCommandResult(command.commandId(), command.nodeId(), true,
                    Optional.of(0), "Accepted typed command " + command.commandType() + ".", now, now, "accepted", "",
                    java.util.Map.of());
        };
    }
}
