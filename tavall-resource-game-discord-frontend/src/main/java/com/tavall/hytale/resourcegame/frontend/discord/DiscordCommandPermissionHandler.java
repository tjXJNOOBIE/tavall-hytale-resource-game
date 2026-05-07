package com.tavall.hytale.resourcegame.frontend.discord;

import com.tavall.hytale.resourcegame.shared.permissions.UniversalPermissionPolicy;
import com.tavall.hytale.resourcegame.shared.permissions.UniversalPermissionSubject;

public final class DiscordCommandPermissionHandler {
    private final DiscordPermissionResolver permissionResolver;
    private final UniversalPermissionPolicy permissionPolicy;

    public DiscordCommandPermissionHandler(
            DiscordPermissionResolver permissionResolver,
            UniversalPermissionPolicy permissionPolicy
    ) {
        this.permissionResolver = permissionResolver;
        this.permissionPolicy = permissionPolicy;
    }

    public boolean canExecute(DiscordPermissionContext context, DiscordCommandLane commandLane) {
        UniversalPermissionSubject subject = permissionResolver.resolveSubject(context);
        return switch (commandLane) {
            case USER -> permissionPolicy.canExecuteUserCommand(subject);
            case ADMIN -> permissionPolicy.canExecuteAdminCommand(subject);
        };
    }
}
