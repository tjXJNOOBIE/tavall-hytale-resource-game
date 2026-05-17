package com.tavall.resourcegame.frontend.minecraft;

import com.tavall.resourcegame.api.internal.permissions.UniversalPermissionPolicy;
import com.tavall.resourcegame.api.internal.permissions.UniversalPermissionSubject;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.Locale;
import java.util.Set;

public final class MinecraftVelocityCommandPermissionHandler implements IMinecraftVelocityCommandPermissionHandler, IMinecraftFrontendDomain, IDependencyInjectableConcrete {
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

    @Override
    public String commandPermission() {
        return getMinecraftProxyConfig().commandPermission();
    }

    @Override
    public String adminPermission() {
        return getMinecraftProxyConfig().adminPermission();
    }

    @Override
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

    @Override
    public boolean canExecute(MinecraftVelocityCommandSource source, String alias, String[] arguments) {
        UniversalPermissionSubject subject = getMinecraftVelocityPermissionResolver().resolveSubject(source);
        if (!source.hasPermission(commandPermission())
                && !source.hasPermission(adminPermission())
                && !source.sourceType().equals("console")
                && !new UniversalPermissionPolicy().canExecuteAdminCommand(subject)) {
            return false;
        }
        if (requiresAdminPermission(alias, arguments)) {
            return new UniversalPermissionPolicy().canExecuteAdminCommand(subject);
        }
        return new UniversalPermissionPolicy().canExecuteUserCommand(subject);
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT);
    }
}
