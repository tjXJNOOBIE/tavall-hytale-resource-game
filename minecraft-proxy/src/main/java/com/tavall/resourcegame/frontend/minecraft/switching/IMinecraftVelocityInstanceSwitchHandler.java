package com.tavall.resourcegame.frontend.minecraft.switching;

import com.tavall.resourcegame.api.internal.frontend.FrontendCommandVerificationResult;
import com.tavall.resourcegame.frontend.minecraft.commands.source.MinecraftVelocityCommandSource;
import com.tavall.resourcegame.frontend.minecraft.routing.MinecraftVelocityCommandResult;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftVelocityInstanceSwitchHandler extends IDependencyInjectableInterface {
    MinecraftVelocityCommandResult dispatchSwitchIfPresent(MinecraftVelocityCommandSource source, FrontendCommandVerificationResult switchRequestResult);
}
