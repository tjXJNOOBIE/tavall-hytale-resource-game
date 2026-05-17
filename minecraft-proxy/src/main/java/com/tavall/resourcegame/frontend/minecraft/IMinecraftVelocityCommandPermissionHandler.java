package com.tavall.resourcegame.frontend.minecraft;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftVelocityCommandPermissionHandler extends IDependencyInjectableInterface {
    String commandPermission();

    String adminPermission();

    boolean requiresAdminPermission(String alias, String[] arguments);

    boolean canExecute(MinecraftVelocityCommandSource source, String alias, String[] arguments);
}
