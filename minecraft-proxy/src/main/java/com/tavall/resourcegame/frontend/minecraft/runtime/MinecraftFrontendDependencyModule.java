package com.tavall.resourcegame.frontend.minecraft.runtime;

import com.tavall.resourcegame.api.internal.frontend.FrontendControlConfig;
import com.tavall.resourcegame.api.internal.frontend.FrontendTcpControlCommandClient;
import com.tavall.resourcegame.api.internal.frontend.IFrontendControlCommandClient;
import com.tavall.resourcegame.api.internal.frontend.IFrontendControlConfig;
import com.tavall.resourcegame.frontend.minecraft.bridge.IMinecraftControlCommandClient;
import com.tavall.resourcegame.frontend.minecraft.bridge.IMinecraftControlPlaneCommandBridge;
import com.tavall.resourcegame.frontend.minecraft.bridge.IMinecraftFrontendCommandEnvelopeFactory;
import com.tavall.resourcegame.frontend.minecraft.bridge.IMinecraftKdCommandEnvelopeBridge;
import com.tavall.resourcegame.frontend.minecraft.bridge.MinecraftControlPlaneCommandBridge;
import com.tavall.resourcegame.frontend.minecraft.bridge.MinecraftDirectControlCommandClient;
import com.tavall.resourcegame.frontend.minecraft.bridge.MinecraftFrontendCommandEnvelopeFactory;
import com.tavall.resourcegame.frontend.minecraft.bridge.MinecraftKdCommandEnvelopeBridge;
import com.tavall.resourcegame.frontend.minecraft.permissions.IMinecraftVelocityCommandPermissionHandler;
import com.tavall.resourcegame.frontend.minecraft.permissions.IMinecraftVelocityPermissionResolver;
import com.tavall.resourcegame.frontend.minecraft.permissions.MinecraftVelocityCommandPermissionHandler;
import com.tavall.resourcegame.frontend.minecraft.permissions.MinecraftVelocityPermissionResolver;
import com.tavall.resourcegame.frontend.minecraft.routing.IMinecraftKdCommandInputFormatterHandler;
import com.tavall.resourcegame.frontend.minecraft.routing.IMinecraftVelocityCommandExecutionHandler;
import com.tavall.resourcegame.frontend.minecraft.routing.MinecraftKdCommandInputFormatterHandler;
import com.tavall.resourcegame.frontend.minecraft.routing.MinecraftVelocityCommandExecutionHandler;
import com.tavall.resourcegame.frontend.minecraft.switching.IMinecraftVelocityInstanceSwitchGateway;
import com.tavall.resourcegame.frontend.minecraft.switching.IMinecraftVelocityInstanceSwitchHandler;
import com.tavall.resourcegame.frontend.minecraft.switching.MinecraftVelocityInstanceSwitchGateway;
import com.tavall.resourcegame.frontend.minecraft.switching.MinecraftVelocityInstanceSwitchHandler;
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
        registerOrReplace(IMinecraftProxyConfig.class, config);
        registerIfMissing(IMinecraftFrontendModule.class, new MinecraftFrontendModule());
        registerIfMissing(IMinecraftFrontendCommandEnvelopeFactory.class, new MinecraftFrontendCommandEnvelopeFactory());
        registerIfMissing(IMinecraftKdCommandInputFormatterHandler.class, new MinecraftKdCommandInputFormatterHandler());
        registerIfMissing(IMinecraftKdCommandEnvelopeBridge.class, new MinecraftKdCommandEnvelopeBridge());
        FrontendControlConfig frontendControlConfig = resolveFrontendControlConfig();
        registerCoreOrReplace(IFrontendControlConfig.class, frontendControlConfig);
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

    private <T> void registerOrReplace(Class<T> token, T instance) {
        if (DependencyLoaderAccess.isInstanceRegistered(token)) {
            DependencyLoaderAccess.replaceInstance(token, () -> instance);
        } else {
            DependencyLoaderAccess.registerInstance(token, instance);
        }
    }

    private <T> void registerCoreIfMissing(Class<T> token, T instance) {
        if (!com.tjxjnoobie.api.dependency.DependencyLoaderAccess.findOptionalInstance(token).isPresent()) {
            com.tjxjnoobie.api.dependency.DependencyLoaderAccess.registerInstance(token, instance);
        }
    }

    private <T> void registerCoreOrReplace(Class<T> token, T instance) {
        if (com.tjxjnoobie.api.dependency.DependencyLoaderAccess.findOptionalInstance(token).isPresent()) {
            com.tjxjnoobie.api.dependency.DependencyLoaderAccess.replaceInstance(token, () -> instance);
        } else {
            com.tjxjnoobie.api.dependency.DependencyLoaderAccess.registerInstance(token, instance);
        }
    }

    private FrontendControlConfig resolveFrontendControlConfig() {
        return com.tjxjnoobie.api.dependency.DependencyLoaderAccess.findOptionalInstance(IFrontendControlConfig.class)
                .map(FrontendControlConfig.class::cast)
                .orElseGet(() -> FrontendControlConfig.fromEnvironment(
                        System.getenv(),
                        URI.create("tcp://127.0.0.1:18081")
                ));
    }
}
