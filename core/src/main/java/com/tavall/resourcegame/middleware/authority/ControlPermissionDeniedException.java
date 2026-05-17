package com.tavall.resourcegame.middleware.authority;

import com.tavall.resourcegame.middleware.control.ControlPermission;

import java.util.UUID;

public final class ControlPermissionDeniedException extends RuntimeException {
    public ControlPermissionDeniedException(UUID principalId, ControlPermission permission, ResourceTarget target) {
        super("Principal " + principalId + " lacks " + permission + " for " + target.primaryScopeType() + ":" + target.primaryScopeId() + ".");
    }
}
