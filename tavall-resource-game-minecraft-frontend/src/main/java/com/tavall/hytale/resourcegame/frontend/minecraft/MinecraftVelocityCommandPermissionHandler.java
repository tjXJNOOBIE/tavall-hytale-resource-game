package com.tavall.hytale.resourcegame.frontend.minecraft;

import com.tavall.hytale.resourcegame.shared.permissions.UniversalPermissionPolicy;
import com.tavall.hytale.resourcegame.shared.permissions.UniversalPermissionSubject;

import java.util.Locale;
import java.util.Set;

public final class MinecraftVelocityCommandPermissionHandler {
    private static final Set<String> ADMIN_OPERATIONS = Set.of(
            "create",
            "spawn",
            "migrate",
            "age",
            "ageall",
            "setstage",
            "setjob",
            "clearjob",
            "train",
            "promote",
            "demote",
            "health",
            "morale",
            "nutrition",
            "housing",
            "maintenance",
            "refresh-cache",
            "refresh-displays",
            "override",
            "clear-override",
            "mode",
            "pause",
            "resume",
            "config",
            "tick",
            "tick-all",
            "set",
            "dry-run",
            "register",
            "switch",
            "confirm",
            "fail",
            "give",
            "heal",
            "wound"
    );

    private final String commandPermission;
    private final String adminPermission;
    private final MinecraftVelocityPermissionResolver permissionResolver;
    private final UniversalPermissionPolicy permissionPolicy;

    public MinecraftVelocityCommandPermissionHandler(String commandPermission, String adminPermission) {
        this(commandPermission, adminPermission, new MinecraftVelocityPermissionResolver(commandPermission, adminPermission), new UniversalPermissionPolicy());
    }

    public MinecraftVelocityCommandPermissionHandler(
            String commandPermission,
            String adminPermission,
            MinecraftVelocityPermissionResolver permissionResolver,
            UniversalPermissionPolicy permissionPolicy
    ) {
        this.commandPermission = commandPermission;
        this.adminPermission = adminPermission;
        this.permissionResolver = permissionResolver;
        this.permissionPolicy = permissionPolicy;
    }

    public String commandPermission() {
        return commandPermission;
    }

    public String adminPermission() {
        return adminPermission;
    }

    public boolean requiresAdminPermission(String alias, String[] arguments) {
        if (arguments.length == 0) {
            return false;
        }
        String category = normalize(arguments[0]);
        String operation = arguments.length > 1 ? normalize(arguments[1]) : "";
        if (category.equals("params") || category.equals("instance") || category.equals("coord")) {
            return true;
        }
        if (category.equals("citizens") || category.equals("citizen") || category.equals("clock") || category.equals("schedule") || category.equals("aging")) {
            return ADMIN_OPERATIONS.contains(operation);
        }
        if (category.equals("kingdom")) {
            return ADMIN_OPERATIONS.contains(operation);
        }
        if (category.equals("resources") || category.equals("resource") || category.equals("troops") || category.equals("troop")) {
            return ADMIN_OPERATIONS.contains(operation);
        }
        return false;
    }

    public boolean canExecute(MinecraftVelocityCommandSource source, String alias, String[] arguments) {
        UniversalPermissionSubject subject = permissionResolver.resolveSubject(source);
        if (!source.hasPermission(commandPermission)
                && !source.hasPermission(adminPermission)
                && !source.sourceType().equals("console")
                && !permissionPolicy.canExecuteAdminCommand(subject)) {
            return false;
        }
        if (requiresAdminPermission(alias, arguments)) {
            return permissionPolicy.canExecuteAdminCommand(subject);
        }
        return permissionPolicy.canExecuteUserCommand(subject);
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT);
    }
}
