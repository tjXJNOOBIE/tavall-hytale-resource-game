package org.tavall.minecraft.routing;

import org.tavall.minecraft.commands.source.MinecraftVelocityCommandSource;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftVelocityCommandExecutionHandler extends IDependencyInjectableInterface {
    MinecraftVelocityCommandResult execute(MinecraftVelocityCommandSource source, String alias, String[] arguments);
}
