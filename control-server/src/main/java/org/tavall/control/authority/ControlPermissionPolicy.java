package org.tavall.control.authority;

import org.tavall.control.runtime.ControlPermission;

import java.util.Objects;

public record ControlPermissionPolicy(
        CloudCommandType commandType,
        ControlAuthorityLevel minimumAuthorityLevel,
        ControlPermission requiredPermission,
        boolean requiresApproval,
        boolean requiresBreakGlass,
        boolean aiExecutionAllowed
) {
    public ControlPermissionPolicy {
        Objects.requireNonNull(commandType, "commandType");
        Objects.requireNonNull(minimumAuthorityLevel, "minimumAuthorityLevel");
        Objects.requireNonNull(requiredPermission, "requiredPermission");
    }
}
