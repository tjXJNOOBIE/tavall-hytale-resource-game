package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.api.internal.frontend.IFrontendControlCommandClient;
import com.tavall.resourcegame.api.internal.frontend.IFrontendControlConfig;
import com.tavall.resourcegame.services.FrontendControlConfig;
import com.tavall.resourcegame.services.FrontendTcpControlCommandClient;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;

import java.net.URI;

/**
 * Registers server-surface handlers after the Bukkit plugin has provided framework-owned objects.
 */
public final class MinecraftBukkitServerDependencyModule {
    public void registerDependencies() {
        registerIfMissing(IMinecraftBukkitServerConfig.class, MinecraftBukkitServerConfig.fromEnvironment(System.getenv()));
        registerIfMissing(IMinecraftBukkitRuntimeState.class, new MinecraftBukkitRuntimeState(System.currentTimeMillis()));
        registerIfMissing(IMinecraftBukkitJsonMapper.class, new MinecraftBukkitJsonMapper());
        registerIfMissing(IMinecraftBukkitJsonHandler.class, new MinecraftBukkitJsonHandler());
        registerIfMissing(IMinecraftBukkitSnapshotHandler.class, new MinecraftBukkitSnapshotHandler());
        registerIfMissing(IMinecraftBukkitSnapshotClientHandler.class, new MinecraftBukkitSnapshotClientHandler());
        registerIfMissing(IMinecraftBukkitCommandClientHandler.class, new MinecraftBukkitCommandClientHandler());
        registerCoreIfMissing(IFrontendControlConfig.class, FrontendControlConfig.fromEnvironment(
                System.getenv(),
                URI.create("tcp://127.0.0.1:18081")
        ));
        registerCoreIfMissing(IFrontendControlCommandClient.class, new FrontendTcpControlCommandClient());
        registerIfMissing(IMinecraftBukkitVisualHandler.class, new MinecraftBukkitVisualHandler());
        registerIfMissing(IMinecraftBukkitWorldActionHandler.class, new MinecraftBukkitWorldActionHandler());
        registerIfMissing(IMinecraftBukkitStructureWorldActionHandler.class, new MinecraftBukkitStructureWorldActionHandler());
        registerIfMissing(IMinecraftBukkitStructureProtectionHandler.class, new MinecraftBukkitStructureProtectionHandler());
        registerIfMissing(IMinecraftBukkitResourcePackHandler.class, new MinecraftBukkitResourcePackHandler());
        registerIfMissing(IMinecraftBukkitPopulationWorldActionHandler.class, new MinecraftBukkitPopulationWorldActionHandler());
        registerIfMissing(IMinecraftBukkitCommandHandler.class, new MinecraftBukkitCommandHandler());
        registerIfMissing(IKingdomCommandRouter.class, new KingdomCommandRouter());
        registerIfMissing(IMinecraftBukkitInventoryUiHandler.class, new MinecraftBukkitInventoryUiHandler());
        registerIfMissing(IMinecraftBukkitInteractionMenuHandler.class, new MinecraftBukkitInteractionMenuHandler());
        registerIfMissing(IMinecraftBukkitInteractionHandler.class, new MinecraftBukkitInteractionHandler());
        registerIfMissing(IMinecraftBukkitInteractionTargetResolver.class, new MinecraftBukkitInteractionTargetResolver());
        registerIfMissing(IMinecraftBukkitInteractionSessionTracker.class, new MinecraftBukkitInteractionSessionTracker());
        registerIfMissing(IMinecraftBukkitPlayerJoinHandler.class, new MinecraftBukkitPlayerJoinHandler());
        registerIfMissing(IMinecraftBukkitTaskScheduler.class, new MinecraftBukkitTaskSchedulerHandler());
        registerIfMissing(IMinecraftBukkitSnapshotSubmitHandler.class, new MinecraftBukkitSnapshotSubmitHandler());
        registerIfMissing(IMinecraftBukkitLogger.class, new MinecraftBukkitLoggerHandler());
    }

    private <T> void registerIfMissing(Class<T> token, T instance) {
        if (!DependencyLoaderAccess.isInstanceRegistered(token)) {
            DependencyLoaderAccess.registerInstance(token, instance);
        }
    }

    private <T> void registerCoreIfMissing(Class<T> token, T instance) {
        if (!com.tjxjnoobie.api.dependency.DependencyLoaderAccess.findOptionalInstance(token).isPresent()) {
            com.tjxjnoobie.api.dependency.DependencyLoaderAccess.registerInstance(token, instance);
        }
    }
}
