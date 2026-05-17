package com.tavall.resourcegame.api.internal.permissions;

import java.util.Objects;

public final class UniversalPermissionPolicy {
    public boolean canExecuteUserCommand(UniversalPermissionSubject subject) {
        return hasPermission(subject, UniversalPermission.EXECUTE_USER_COMMAND);
    }

    public boolean canExecuteAdminCommand(UniversalPermissionSubject subject) {
        return hasPermission(subject, UniversalPermission.EXECUTE_ADMIN_COMMAND);
    }

    public boolean hasPermission(UniversalPermissionSubject subject, UniversalPermission permission) {
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(permission, "permission");
        return subject.hasPermission(permission);
    }

    public boolean hasPowerAtLeast(UniversalPermissionSubject subject, UniversalPermissionRole role) {
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(role, "role");
        return subject.role().powerLevel() >= role.powerLevel();
    }
}
