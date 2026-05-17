package com.tavall.resourcegame.frontend.minecraft;

import com.tavall.resourcegame.shared.permissions.UniversalPermissionRole;
import com.tavall.resourcegame.shared.permissions.UniversalPermissionSubject;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftVelocityPermissionResolver extends IDependencyInjectableInterface {
    UniversalPermissionSubject resolveSubject(MinecraftVelocityCommandSource source);

    UniversalPermissionRole resolveRole(MinecraftVelocityCommandSource source);
}
