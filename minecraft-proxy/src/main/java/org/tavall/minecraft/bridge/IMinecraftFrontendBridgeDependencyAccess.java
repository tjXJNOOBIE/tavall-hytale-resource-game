package org.tavall.minecraft.bridge;

import org.tavall.dependency.DependencyLoaderAccess;
import org.tavall.minecraft.runtime.IMinecraftFrontendModule;
import org.tavall.minecraft.runtime.IMinecraftProxyConfig;
import org.tavall.minecraft.routing.IMinecraftKdCommandInputFormatterHandler;

public interface IMinecraftFrontendBridgeDependencyAccess {
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
}
