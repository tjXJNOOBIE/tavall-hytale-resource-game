package com.tavall.resourcegame.frontend.minecraft.permissions;

import com.tavall.resourcegame.api.internal.permissions.UniversalPermissionRole;
import com.tavall.resourcegame.api.internal.permissions.UniversalPermissionSubject;
import com.tavall.resourcegame.frontend.minecraft.commands.source.MinecraftVelocityCommandSource;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftVelocityPermissionResolver extends IDependencyInjectableInterface {
    UniversalPermissionSubject resolveSubject(MinecraftVelocityCommandSource source);

    UniversalPermissionRole resolveRole(MinecraftVelocityCommandSource source);
}
