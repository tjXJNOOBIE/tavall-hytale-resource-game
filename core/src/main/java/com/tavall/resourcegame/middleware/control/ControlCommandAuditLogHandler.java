package com.tavall.resourcegame.middleware.control;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class ControlCommandAuditLogHandler implements IControlCommandDomain, IDependencyInjectableConcrete {
    public ControlCommandAuditLog logCompletedCommand(ControlCommand command, ControlCommandResult result, Instant completedAt) {
        ControlCommandAuditLog auditLog = new ControlCommandAuditLog(
                UUID.randomUUID(),
                command.commandId(),
                command.commandType(),
                command.issuedBy().displayName(),
                command.issuedFrom(),
                command.targetScope(),
                command.targetPlatforms(),
                getControlCommandSerializer().redactedArguments(command),
                command.dryRun(),
                result.state(),
                result.success(),
                result.message(),
                command.createdAt(),
                completedAt,
                Map.of("operatorRole", command.issuedBy().role().name())
        );
        return getControlCommandAuditLogRepository().saveAuditLog(auditLog);
    }
}
