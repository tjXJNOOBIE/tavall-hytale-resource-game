package org.tavall.control.authority;

import org.tavall.control.runtime.ControlPermission;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record ControlAuthority(
        UUID authorityId,
        UUID principalId,
        ControlAuthorityLevel authorityLevel,
        AuthorityScope scope,
        Set<ControlPermission> permissions,
        Set<AuthorityCondition> conditions,
        long grantedAtEpochMillis,
        long expiresAtEpochMillis,
        UUID grantedBy,
        boolean breakGlassAllowed,
        boolean delegationAllowed,
        boolean enabled
) {
    public ControlAuthority {
        Objects.requireNonNull(authorityId, "authorityId");
        Objects.requireNonNull(principalId, "principalId");
        Objects.requireNonNull(authorityLevel, "authorityLevel");
        Objects.requireNonNull(scope, "scope");
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
        conditions = conditions == null ? Set.of() : Set.copyOf(conditions);
    }

    public static ControlAuthority enabled(
            UUID principalId,
            ControlAuthorityLevel authorityLevel,
            AuthorityScope scope,
            Set<ControlPermission> permissions,
            UUID grantedBy,
            long grantedAtEpochMillis,
            boolean breakGlassAllowed,
            boolean delegationAllowed
    ) {
        return new ControlAuthority(
                UUID.randomUUID(),
                principalId,
                authorityLevel,
                scope,
                permissions,
                Set.of(),
                grantedAtEpochMillis,
                0L,
                grantedBy,
                breakGlassAllowed,
                delegationAllowed,
                true
        );
    }

    public boolean hasPermission(ControlPermission permission) {
        return permissions.contains(permission);
    }

    public boolean isExpired(long nowEpochMillis) {
        return expiresAtEpochMillis > 0 && nowEpochMillis > expiresAtEpochMillis;
    }
}
