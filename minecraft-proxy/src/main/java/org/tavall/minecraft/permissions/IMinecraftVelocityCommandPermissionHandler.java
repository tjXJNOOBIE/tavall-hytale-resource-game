package org.tavall.minecraft.permissions;

import org.tavall.minecraft.commands.source.MinecraftVelocityCommandSource;
import org.tavall.dependency.IDependencyInjectableInterface;

public interface IMinecraftVelocityCommandPermissionHandler extends IDependencyInjectableInterface {
    String commandPermission();

    String adminPermission();

    boolean requiresAdminPermission(String alias, String[] arguments);

    boolean canExecute(MinecraftVelocityCommandSource source, String alias, String[] arguments);
}
