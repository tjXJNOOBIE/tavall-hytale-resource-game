package org.tavall.minecraft.permissions;

import org.tavall.api.minecraft.cache.KingdomCacheGateway;
import org.tavall.minecraft.bootstrap.MinecraftProxyConfig;
import org.tavall.dependency.DependencyLoaderAccess;

import java.util.Objects;

public interface IMinecraftFrontendPermissionDependencyAccess {
    default MinecraftProxyConfig getMinecraftProxyConfig() {
        return Objects.requireNonNull(DependencyLoaderAccess.findInstance(MinecraftProxyConfig.class),
                "Expected MinecraftProxyConfig to be registered in the DI map.");
    }

    default KingdomCacheGateway getKingdomCacheGateway() {
        return Objects.requireNonNull(DependencyLoaderAccess.findInstance(KingdomCacheGateway.class),
                "Expected KingdomCacheGateway to be registered in the DI map.");
    }

    default IMinecraftVelocityPermissionResolver getMinecraftVelocityPermissionResolver() {
        return Objects.requireNonNull(DependencyLoaderAccess.findInstance(IMinecraftVelocityPermissionResolver.class),
                "Expected IMinecraftVelocityPermissionResolver to be registered in the DI map.");
    }

    default IMinecraftVelocityCommandPermissionHandler getMinecraftVelocityCommandPermissionHandler() {
        return Objects.requireNonNull(DependencyLoaderAccess.findInstance(IMinecraftVelocityCommandPermissionHandler.class),
                "Expected IMinecraftVelocityCommandPermissionHandler to be registered in the DI map.");
    }
}

