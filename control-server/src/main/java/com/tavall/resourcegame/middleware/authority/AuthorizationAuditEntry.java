package com.tavall.resourcegame.middleware.authority;

import java.util.Objects;
import java.util.UUID;

public record AuthorizationAuditEntry(
        UUID auditEntryId,
        UUID requestId,
        UUID principalId,
        CloudCommandType commandType,
        boolean allowed,
        String message,
        long auditedAtEpochMillis
) {
    public AuthorizationAuditEntry {
        Objects.requireNonNull(auditEntryId, "auditEntryId");
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(principalId, "principalId");
        Objects.requireNonNull(commandType, "commandType");
        message = message == null ? "" : message;
    }
}
