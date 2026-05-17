package com.tavall.resourcegame.middleware.authority;

import com.tavall.resourcegame.middleware.control.ControlPermission;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record AuthorityGrantRequest(
        UUID grantedToPrincipalId,
        ControlAuthorityLevel authorityLevel,
        AuthorityScope scope,
        Set<ControlPermission> permissions,
        long expiresAtEpochMillis,
        String reason
) {
    public AuthorityGrantRequest {
        Objects.requireNonNull(grantedToPrincipalId, "grantedToPrincipalId");
        Objects.requireNonNull(authorityLevel, "authorityLevel");
        scope = scope == null ? AuthorityScope.global() : scope;
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
        reason = reason == null ? "" : reason;
    }
}
