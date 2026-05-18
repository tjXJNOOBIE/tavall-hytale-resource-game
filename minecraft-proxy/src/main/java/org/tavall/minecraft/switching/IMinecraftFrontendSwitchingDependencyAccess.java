package org.tavall.minecraft.switching;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.minecraft.runtime.IMinecraftVelocityProxyServer;

import java.util.Optional;

public interface IMinecraftFrontendSwitchingDependencyAccess {
    default IMinecraftVelocityInstanceSwitchGateway getMinecraftVelocityInstanceSwitchGateway() {
        return DependencyLoaderAccess.requireInstance(IMinecraftVelocityInstanceSwitchGateway.class);
    }

    default IMinecraftVelocityInstanceSwitchHandler getMinecraftVelocityInstanceSwitchHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftVelocityInstanceSwitchHandler.class);
    }

    default IMinecraftVelocityProxyServer getMinecraftVelocityProxyServer() {
        return DependencyLoaderAccess.requireInstance(IMinecraftVelocityProxyServer.class);
    }

    default Optional<IMinecraftVelocityProxyServer> findMinecraftVelocityProxyServer() {
        return DependencyLoaderAccess.findOptionalInstance(IMinecraftVelocityProxyServer.class);
    }
}
