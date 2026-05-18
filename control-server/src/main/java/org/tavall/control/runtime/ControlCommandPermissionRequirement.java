package org.tavall.control.runtime;

import java.util.Objects;

public record ControlCommandPermissionRequirement(
        ControlPermission permission,
        boolean highRisk
) {
    public ControlCommandPermissionRequirement {
        Objects.requireNonNull(permission, "permission");
    }
}
