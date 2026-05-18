package org.tavall.control.authority;

import java.util.Objects;
import java.util.UUID;

public record BreakGlassSession(
        UUID sessionId,
        UUID activatedBy,
        ControlAuthorityLevel authorityLevel,
        AuthorityScope scope,
        String reason,
        long activatedAtEpochMillis,
        long expiresAtEpochMillis,
        boolean active
) {
    public BreakGlassSession {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(activatedBy, "activatedBy");
        Objects.requireNonNull(authorityLevel, "authorityLevel");
        scope = scope == null ? AuthorityScope.global() : scope;
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason is required for break-glass sessions");
        }
    }

    public boolean activeAt(long nowEpochMillis) {
        return active && expiresAtEpochMillis > nowEpochMillis;
    }
}
