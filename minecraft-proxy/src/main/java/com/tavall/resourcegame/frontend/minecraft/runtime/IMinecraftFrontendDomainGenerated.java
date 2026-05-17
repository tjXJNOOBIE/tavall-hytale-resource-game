package com.tavall.resourcegame.frontend.minecraft.runtime;

import com.tavall.resourcegame.frontend.minecraft.bridge.IMinecraftControlCommandClient;
import com.tavall.resourcegame.frontend.minecraft.bridge.IMinecraftControlPlaneCommandBridge;
import com.tavall.resourcegame.frontend.minecraft.bridge.IMinecraftFrontendCommandEnvelopeFactory;
import com.tavall.resourcegame.frontend.minecraft.bridge.IMinecraftKdCommandEnvelopeBridge;
import com.tavall.resourcegame.frontend.minecraft.permissions.IMinecraftVelocityCommandPermissionHandler;
import com.tavall.resourcegame.frontend.minecraft.permissions.IMinecraftVelocityPermissionResolver;
import com.tavall.resourcegame.frontend.minecraft.routing.IMinecraftKdCommandInputFormatterHandler;
import com.tavall.resourcegame.frontend.minecraft.routing.IMinecraftVelocityCommandExecutionHandler;
import com.tavall.resourcegame.frontend.minecraft.switching.IMinecraftVelocityInstanceSwitchGateway;
import com.tavall.resourcegame.frontend.minecraft.switching.IMinecraftVelocityInstanceSwitchHandler;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;

public interface IMinecraftFrontendDomainGenerated {
    default IMinecraftProxyConfig getMinecraftProxyConfig() {
        return DependencyLoaderAccess.requireInstance(IMinecraftProxyConfig.class);
    }

    default IMinecraftFrontendModule getMinecraftFrontendModule() {
        return DependencyLoaderAccess.requireInstance(IMinecraftFrontendModule.class);
    }

    default IMinecraftFrontendCommandEnvelopeFactory getMinecraftFrontendCommandEnvelopeFactory() {
        return DependencyLoaderAccess.requireInstance(IMinecraftFrontendCommandEnvelopeFactory.class);
    }

    default IMinecraftKdCommandInputFormatterHandler getMinecraftKdCommandInputFormatterHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftKdCommandInputFormatterHandler.class);
    }

    default IMinecraftKdCommandEnvelopeBridge getMinecraftKdCommandEnvelopeBridge() {
        return DependencyLoaderAccess.requireInstance(IMinecraftKdCommandEnvelopeBridge.class);
    }

    default IMinecraftControlCommandClient getMinecraftControlCommandClient() {
        return DependencyLoaderAccess.requireInstance(IMinecraftControlCommandClient.class);
    }

    default IMinecraftControlPlaneCommandBridge getMinecraftControlPlaneCommandBridge() {
        return DependencyLoaderAccess.requireInstance(IMinecraftControlPlaneCommandBridge.class);
    }

    default IMinecraftVelocityPermissionResolver getMinecraftVelocityPermissionResolver() {
        return DependencyLoaderAccess.requireInstance(IMinecraftVelocityPermissionResolver.class);
    }

    default IMinecraftVelocityCommandPermissionHandler getMinecraftVelocityCommandPermissionHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftVelocityCommandPermissionHandler.class);
    }

    default IMinecraftVelocityInstanceSwitchGateway getMinecraftVelocityInstanceSwitchGateway() {
        return DependencyLoaderAccess.requireInstance(IMinecraftVelocityInstanceSwitchGateway.class);
    }

    default IMinecraftVelocityInstanceSwitchHandler getMinecraftVelocityInstanceSwitchHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftVelocityInstanceSwitchHandler.class);
    }

    default IMinecraftVelocityCommandExecutionHandler getMinecraftVelocityCommandExecutionHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftVelocityCommandExecutionHandler.class);
    }

    default IMinecraftVelocityProxyServer getMinecraftVelocityProxyServer() {
        return DependencyLoaderAccess.requireInstance(IMinecraftVelocityProxyServer.class);
    }
}
