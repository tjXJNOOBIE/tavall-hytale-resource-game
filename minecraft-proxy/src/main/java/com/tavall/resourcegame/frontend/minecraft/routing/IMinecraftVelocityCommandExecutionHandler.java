package com.tavall.resourcegame.frontend.minecraft.routing;

import com.tavall.resourcegame.frontend.minecraft.commands.source.MinecraftVelocityCommandSource;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftVelocityCommandExecutionHandler extends IDependencyInjectableInterface {
    MinecraftVelocityCommandResult execute(MinecraftVelocityCommandSource source, String alias, String[] arguments);
}
