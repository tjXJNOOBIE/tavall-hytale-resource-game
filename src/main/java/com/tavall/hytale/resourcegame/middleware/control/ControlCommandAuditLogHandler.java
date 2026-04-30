package com.tavall.hytale.resourcegame.middleware.control;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class ControlCommandAuditLogHandler {
    private final ControlCommandAuditLogRepository auditLogRepository;
    private final ControlCommandSerializer serializer;

    public ControlCommandAuditLogHandler(ControlCommandAuditLogRepository auditLogRepository, ControlCommandSerializer serializer) {
        this.auditLogRepository = auditLogRepository;
        this.serializer = serializer;
    }

    public ControlCommandAuditLog logCompletedCommand(ControlCommand command, ControlCommandResult result, Instant completedAt) {
        ControlCommandAuditLog auditLog = new ControlCommandAuditLog(
                UUID.randomUUID(),
                command.commandId(),
                command.commandType(),
                command.issuedBy().displayName(),
                command.issuedFrom(),
                command.targetScope(),
                command.targetPlatforms(),
                serializer.redactedArguments(command),
                command.dryRun(),
                result.state(),
                result.success(),
                result.message(),
                command.createdAt(),
                completedAt,
                Map.of("operatorRole", command.issuedBy().role().name())
        );
        return auditLogRepository.saveAuditLog(auditLog);
    }
}
