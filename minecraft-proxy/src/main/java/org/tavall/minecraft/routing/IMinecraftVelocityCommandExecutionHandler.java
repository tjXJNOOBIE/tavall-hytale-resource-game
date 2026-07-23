package org.tavall.minecraft.routing;

import org.tavall.dependency.IDependencyInjectableInterface;
import org.tavall.minecraft.commands.source.MinecraftVelocityCommandSource;

public interface IMinecraftVelocityCommandExecutionHandler extends IDependencyInjectableInterface {
    MinecraftVelocityCommandResult execute(MinecraftVelocityCommandSource source, String alias, String[] arguments);
}
