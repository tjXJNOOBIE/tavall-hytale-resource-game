package org.tavall.control.runtime;

import java.util.EnumSet;
import java.util.Set;

public enum ControlOperatorRole {
    VIEWER(EnumSet.of(ControlPermission.VIEW_CONTROL_PANEL, ControlPermission.VIEW_AUDIT_LOG)),
    OPERATOR(EnumSet.of(
            ControlPermission.VIEW_CONTROL_PANEL,
            ControlPermission.VIEW_AUDIT_LOG,
            ControlPermission.EXECUTE_DEBUG_COMMAND,
            ControlPermission.EXECUTE_GAMEPLAY_COMMAND,
            ControlPermission.MANAGE_PLAYER_STATE,
            ControlPermission.MANAGE_TROOP_STATE,
            ControlPermission.MANAGE_HEALING_STATE
    )),
    ADMIN(EnumSet.complementOf(EnumSet.of(ControlPermission.MANAGE_CONTROL_OPERATORS))),
    OWNER(EnumSet.allOf(ControlPermission.class)),
    SYSTEM(EnumSet.allOf(ControlPermission.class));

    private final Set<ControlPermission> permissions;

    ControlOperatorRole(Set<ControlPermission> permissions) {
        this.permissions = Set.copyOf(permissions);
    }

    public boolean hasPermission(ControlPermission permission) {
        return permissions.contains(permission);
    }

    public boolean elevatedForHighRiskCommand() {
        return this == ADMIN || this == OWNER || this == SYSTEM;
    }

    public Set<ControlPermission> permissions() {
        return permissions;
    }
}
