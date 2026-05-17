package com.tavall.resourcegame.frontend.discord;

import com.tavall.resourcegame.shared.permissions.UniversalPermissionPolicy;
import com.tavall.resourcegame.shared.permissions.UniversalPermissionSubject;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

public final class DiscordCommandPermissionHandler implements IDiscordCommandPermissionHandler, IDiscordFrontendDomain, IDependencyInjectableConcrete {
    @Override
    public boolean canExecute(DiscordPermissionContext context, DiscordCommandLane commandLane) {
        UniversalPermissionSubject subject = getDiscordPermissionResolver().resolveSubject(context);
        return switch (commandLane) {
            case USER -> new UniversalPermissionPolicy().canExecuteUserCommand(subject);
            case ADMIN -> new UniversalPermissionPolicy().canExecuteAdminCommand(subject);
        };
    }
}
