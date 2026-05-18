package org.tavall.control.runtime;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class ControlCommandAuditLogHandler implements ControlCommandDomain, IDependencyInjectableConcrete {
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
