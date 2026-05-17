package com.tavall.resourcegame.middleware.control;

import java.util.Objects;

public record ControlCommandPermissionRequirement(
        ControlPermission permission,
        boolean highRisk
) {
    public ControlCommandPermissionRequirement {
        Objects.requireNonNull(permission, "permission");
    }
}
