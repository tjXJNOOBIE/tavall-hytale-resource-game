package org.tavall.control.authority;

import org.tavall.control.runtime.ControlPermission;

import java.util.UUID;

public final class ControlPermissionDeniedException extends RuntimeException {
    public ControlPermissionDeniedException(UUID principalId, ControlPermission permission, ResourceTarget target) {
        super("Principal " + principalId + " lacks " + permission + " for " + target.primaryScopeType() + ":" + target.primaryScopeId() + ".");
    }
}
