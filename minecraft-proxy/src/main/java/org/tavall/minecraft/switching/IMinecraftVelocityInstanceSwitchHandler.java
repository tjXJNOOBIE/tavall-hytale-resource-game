package org.tavall.minecraft.switching;

import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.minecraft.commands.source.MinecraftVelocityCommandSource;
import org.tavall.minecraft.routing.MinecraftVelocityCommandResult;
import org.tavall.dependency.IDependencyInjectableInterface;

public interface IMinecraftVelocityInstanceSwitchHandler extends IDependencyInjectableInterface {
    MinecraftVelocityCommandResult dispatchSwitchIfPresent(MinecraftVelocityCommandSource source, FrontendCommandVerificationResult switchRequestResult);
}
