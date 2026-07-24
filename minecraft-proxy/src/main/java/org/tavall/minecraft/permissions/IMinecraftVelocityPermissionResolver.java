package org.tavall.minecraft.permissions;

import org.tavall.api.minecraft.permissions.UniversalPermissionRole;
import org.tavall.api.minecraft.permissions.UniversalPermissionSubject;
import org.tavall.minecraft.commands.source.MinecraftVelocityCommandSource;
import org.tavall.dependency.IDependencyInjectableInterface;

public interface IMinecraftVelocityPermissionResolver extends IDependencyInjectableInterface {
    UniversalPermissionSubject resolveSubject(MinecraftVelocityCommandSource source);

    UniversalPermissionRole resolveRole(MinecraftVelocityCommandSource source);
}
