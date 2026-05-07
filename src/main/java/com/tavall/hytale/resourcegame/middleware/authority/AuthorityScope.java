package com.tavall.hytale.resourcegame.middleware.authority;

import java.util.Objects;

public record AuthorityScope(
        AuthorityScopeType scopeType,
        String scopeId
) {
    public AuthorityScope {
        Objects.requireNonNull(scopeType, "scopeType");
        scopeId = scopeId == null || scopeId.isBlank() ? "*" : scopeId;
    }

    public static AuthorityScope global() {
        return new AuthorityScope(AuthorityScopeType.GLOBAL, "*");
    }

    public boolean matches(ResourceTarget target) {
        if (scopeType == AuthorityScopeType.GLOBAL) {
            return true;
        }
        return target.matchesScope(scopeType, scopeId);
    }
}
