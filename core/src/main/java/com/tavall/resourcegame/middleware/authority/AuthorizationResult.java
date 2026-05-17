package com.tavall.resourcegame.middleware.authority;

import java.util.Optional;
import java.util.UUID;

public record AuthorizationResult(
        UUID principalId,
        CloudCommandType commandType,
        boolean allowed,
        String message,
        Optional<ControlAuthority> matchedAuthority
) {
    public static AuthorizationResult allowed(UUID principalId, CloudCommandType commandType, ControlAuthority authority) {
        return new AuthorizationResult(principalId, commandType, true, "Authorized.", Optional.of(authority));
    }

    public static AuthorizationResult denied(UUID principalId, CloudCommandType commandType, String message) {
        return new AuthorizationResult(principalId, commandType, false, message, Optional.empty());
    }
}
