package org.tavall.minecraft.runtime;

import org.tavall.api.minecraft.frontend.FrontendControlConfig;
import org.tavall.api.minecraft.frontend.FrontendTcpControlCommandClient;
import org.tavall.api.minecraft.frontend.IFrontendControlCommandClient;
import org.tavall.api.minecraft.frontend.IFrontendControlConfig;
import org.tavall.minecraft.bridge.IMinecraftControlCommandClient;
import org.tavall.minecraft.bridge.IMinecraftControlPlaneCommandBridge;
import org.tavall.minecraft.bridge.IMinecraftFrontendCommandEnvelopeFactory;
import org.tavall.minecraft.bridge.IMinecraftKdCommandEnvelopeBridge;
import org.tavall.minecraft.bridge.MinecraftControlPlaneCommandBridge;
import org.tavall.minecraft.bridge.MinecraftDirectControlCommandClient;
import org.tavall.minecraft.bridge.MinecraftFrontendCommandEnvelopeFactory;
import org.tavall.minecraft.bridge.MinecraftKdCommandEnvelopeBridge;
import org.tavall.minecraft.permissions.IMinecraftVelocityCommandPermissionHandler;
import org.tavall.minecraft.permissions.IMinecraftVelocityPermissionResolver;
import org.tavall.minecraft.permissions.MinecraftVelocityCommandPermissionHandler;
import org.tavall.minecraft.permissions.MinecraftVelocityPermissionResolver;
import org.tavall.minecraft.routing.IMinecraftKdCommandInputFormatterHandler;
import org.tavall.minecraft.routing.IMinecraftVelocityCommandExecutionHandler;
import org.tavall.minecraft.routing.MinecraftKdCommandInputFormatterHandler;
import org.tavall.minecraft.routing.MinecraftVelocityCommandExecutionHandler;
import org.tavall.minecraft.switching.IMinecraftVelocityInstanceSwitchGateway;
import org.tavall.minecraft.switching.IMinecraftVelocityInstanceSwitchHandler;
import org.tavall.minecraft.switching.MinecraftVelocityInstanceSwitchGateway;
import org.tavall.minecraft.switching.MinecraftVelocityInstanceSwitchHandler;
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
