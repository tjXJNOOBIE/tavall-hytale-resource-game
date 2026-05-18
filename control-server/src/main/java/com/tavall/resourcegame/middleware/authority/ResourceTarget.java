package org.tavall.control.authority;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public record ResourceTarget(
        AuthorityScopeType primaryScopeType,
        String primaryScopeId,
        Map<AuthorityScopeType, String> scopePath
) {
    public ResourceTarget {
        Objects.requireNonNull(primaryScopeType, "primaryScopeType");
        primaryScopeId = primaryScopeId == null || primaryScopeId.isBlank() ? "*" : primaryScopeId;
        EnumMap<AuthorityScopeType, String> normalizedPath = new EnumMap<>(AuthorityScopeType.class);
        if (scopePath != null) {
            normalizedPath.putAll(scopePath);
        }
        normalizedPath.put(primaryScopeType, primaryScopeId);
        scopePath = Map.copyOf(normalizedPath);
    }

    public static ResourceTarget global() {
        return new ResourceTarget(AuthorityScopeType.GLOBAL, "*", Map.of(AuthorityScopeType.GLOBAL, "*"));
    }

    public static ResourceTarget of(AuthorityScopeType scopeType, String scopeId) {
        return new ResourceTarget(scopeType, scopeId, Map.of(scopeType, scopeId));
    }

    public boolean matchesScope(AuthorityScopeType scopeType, String scopeId) {
        if (scopeType == AuthorityScopeType.GLOBAL) {
            return true;
        }
        String value = scopePath.get(scopeType);
        return value != null && value.equals(scopeId);
    }
}
