package org.tavall.minecraft.permissions;

import org.tavall.minecraft.commands.source.MinecraftVelocityCommandSource;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftVelocityCommandPermissionHandler extends IDependencyInjectableInterface {
    String commandPermission();

    String adminPermission();

    boolean requiresAdminPermission(String alias, String[] arguments);

    boolean canExecute(MinecraftVelocityCommandSource source, String alias, String[] arguments);
}
