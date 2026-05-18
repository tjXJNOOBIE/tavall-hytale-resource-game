package org.tavall.control.runtime;

import org.tavall.control.common.GamePlatform;
import org.tavall.control.common.MetadataMaps;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record ControlCommandAuditLog(
        UUID auditLogId,
        ControlCommandId commandId,
        ControlCommandType commandType,
        String issuedBy,
        CommandIssuedFrom issuedFrom,
        CommandTargetScope targetScope,
        Set<GamePlatform> targetPlatforms,
        Map<String, String> argumentsRedacted,
        boolean dryRun,
        CommandExecutionState resultState,
        boolean success,
        String message,
        Instant createdAt,
        Instant completedAt,
        Map<String, String> metadata
) {
    public ControlCommandAuditLog {
        Objects.requireNonNull(auditLogId, "auditLogId");
        Objects.requireNonNull(commandId, "commandId");
        Objects.requireNonNull(commandType, "commandType");
        issuedBy = issuedBy == null ? "" : issuedBy;
        issuedFrom = issuedFrom == null ? CommandIssuedFrom.SYSTEM : issuedFrom;
        targetScope = targetScope == null ? CommandTargetScope.GLOBAL : targetScope;
        targetPlatforms = targetPlatforms == null ? Set.of() : Set.copyOf(targetPlatforms);
        argumentsRedacted = argumentsRedacted == null ? Map.of() : Map.copyOf(argumentsRedacted);
        message = message == null ? "" : message;
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(completedAt, "completedAt");
        metadata = MetadataMaps.immutable(metadata);
    }
}
