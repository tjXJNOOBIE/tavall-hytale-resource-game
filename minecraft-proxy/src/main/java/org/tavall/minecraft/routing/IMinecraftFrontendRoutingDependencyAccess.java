package org.tavall.minecraft.routing;

import org.tavall.dependency.DependencyLoaderAccess;

public interface IMinecraftFrontendRoutingDependencyAccess {
    default IMinecraftVelocityCommandExecutionHandler getMinecraftVelocityCommandExecutionHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftVelocityCommandExecutionHandler.class);
    }
}
