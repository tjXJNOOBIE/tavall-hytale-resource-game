package com.tavall.resourcegame.frontend.discord;

import com.tavall.resourcegame.shared.permissions.UniversalPermissionRole;
import com.tavall.resourcegame.shared.permissions.UniversalPermissionSubject;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IDiscordPermissionResolver extends IDependencyInjectableInterface {
    UniversalPermissionSubject resolveSubject(DiscordPermissionContext context);

    UniversalPermissionRole resolveRole(DiscordPermissionContext context);
}
