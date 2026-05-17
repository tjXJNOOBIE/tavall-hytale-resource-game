package com.tavall.resourcegame.frontend.minecraft;

import com.tavall.resourcegame.dependency.interfaces.IFrontendControlCommandClient;
import com.tavall.resourcegame.dependency.interfaces.IFrontendControlConfig;
import com.tavall.resourcegame.services.FrontendControlConfig;
import com.tavall.resourcegame.services.FrontendTcpControlCommandClient;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;

import java.net.URI;

/**
 * Registers Velocity frontend collaborators behind Tavall DI tokens while the Velocity plugin
 * remains the only class constructed by the proxy runtime.
 */
public final class MinecraftFrontendDependencyModule {
    public void registerDependencies() {
        registerDependencies(MinecraftProxyConfig.fromEnvironment(System.getenv()), MinecraftVelocityInstanceSwitchGateway.noop());
    }

    public void registerDependencies(IMinecraftProxyConfig config, MinecraftVelocityInstanceSwitchGateway switchGateway) {
        registerIfMissing(IMinecraftProxyConfig.class, config);
        registerIfMissing(IMinecraftFrontendModule.class, new MinecraftFrontendModule());
        registerIfMissing(IMinecraftFrontendCommandEnvelopeFactory.class, new MinecraftFrontendCommandEnvelopeFactory());
        registerIfMissing(IMinecraftKdCommandInputFormatterHandler.class, new MinecraftKdCommandInputFormatterHandler());
        registerIfMissing(IMinecraftKdCommandEnvelopeBridge.class, new MinecraftKdCommandEnvelopeBridge());
        FrontendControlConfig frontendControlConfig = resolveFrontendControlConfig();
        registerCoreIfMissing(IFrontendControlConfig.class, frontendControlConfig);
        registerCoreIfMissing(IFrontendControlCommandClient.class, new FrontendTcpControlCommandClient());
        registerIfMissing(IMinecraftControlCommandClient.class, new MinecraftDirectControlCommandClient());
        registerIfMissing(IMinecraftControlPlaneCommandBridge.class, new MinecraftControlPlaneCommandBridge());
        registerIfMissing(IMinecraftVelocityPermissionResolver.class, new MinecraftVelocityPermissionResolver());
        registerIfMissing(IMinecraftVelocityCommandPermissionHandler.class, new MinecraftVelocityCommandPermissionHandler());
        registerIfMissing(IMinecraftVelocityInstanceSwitchGateway.class, switchGateway);
        registerIfMissing(IMinecraftVelocityInstanceSwitchHandler.class, new MinecraftVelocityInstanceSwitchHandler());
        registerIfMissing(IMinecraftVelocityCommandExecutionHandler.class, new MinecraftVelocityCommandExecutionHandler());
    }

    private <T> void registerIfMissing(Class<T> token, T instance) {
        if (!DependencyLoaderAccess.isInstanceRegistered(token)) {
            DependencyLoaderAccess.registerInstance(token, instance);
        }
    }

    private <T> void registerCoreIfMissing(Class<T> token, T instance) {
        if (!com.tavall.resourcegame.dependency.DependencyLoaderAccess.findOptionalInstance(token).isPresent()) {
            com.tavall.resourcegame.dependency.DependencyLoaderAccess.registerInstance(token, instance);
        }
    }

    private FrontendControlConfig resolveFrontendControlConfig() {
        return com.tavall.resourcegame.dependency.DependencyLoaderAccess.findOptionalInstance(IFrontendControlConfig.class)
                .map(FrontendControlConfig.class::cast)
                .orElseGet(() -> FrontendControlConfig.fromEnvironment(
                        System.getenv(),
                        URI.create("tcp://127.0.0.1:18081")
                ));
    }
}
