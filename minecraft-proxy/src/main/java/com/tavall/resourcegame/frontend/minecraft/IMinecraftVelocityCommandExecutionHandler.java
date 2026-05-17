package com.tavall.resourcegame.frontend.minecraft;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftVelocityCommandExecutionHandler extends IDependencyInjectableInterface {
    MinecraftVelocityCommandResult execute(MinecraftVelocityCommandSource source, String alias, String[] arguments);
}
