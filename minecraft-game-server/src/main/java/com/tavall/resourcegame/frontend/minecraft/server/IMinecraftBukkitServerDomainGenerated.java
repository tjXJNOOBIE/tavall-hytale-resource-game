package com.tavall.resourcegame.frontend.minecraft.server;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;

public interface IMinecraftBukkitServerDomainGenerated {
    default IMinecraftBukkitServerConfig getMinecraftBukkitServerConfig() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitServerConfig.class);
    }

    default IMinecraftBukkitRuntimeState getMinecraftBukkitRuntimeState() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitRuntimeState.class);
    }

    default IMinecraftBukkitJsonHandler getMinecraftBukkitJsonHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitJsonHandler.class);
    }

    default IMinecraftBukkitJsonMapper getMinecraftBukkitJsonMapper() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitJsonMapper.class);
    }

    default IMinecraftBukkitSnapshotHandler getMinecraftBukkitSnapshotHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitSnapshotHandler.class);
    }

    default IMinecraftBukkitSnapshotClientHandler getMinecraftBukkitSnapshotClientHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitSnapshotClientHandler.class);
    }

    default IMinecraftBukkitCommandClientHandler getMinecraftBukkitCommandClientHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitCommandClientHandler.class);
    }

    default IMinecraftBukkitCommandHandler getMinecraftBukkitCommandHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitCommandHandler.class);
    }

    default IMinecraftBukkitInteractionHandler getMinecraftBukkitInteractionHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitInteractionHandler.class);
    }

    default IMinecraftBukkitInteractionTargetResolver getMinecraftBukkitInteractionTargetResolver() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitInteractionTargetResolver.class);
    }

    default IMinecraftBukkitInteractionSessionTracker getMinecraftBukkitInteractionSessionTracker() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitInteractionSessionTracker.class);
    }

    default IMinecraftBukkitPlayerJoinHandler getMinecraftBukkitPlayerJoinHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitPlayerJoinHandler.class);
    }

    default IMinecraftBukkitTaskScheduler getMinecraftBukkitTaskScheduler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitTaskScheduler.class);
    }

    default IMinecraftBukkitSnapshotSubmitHandler getMinecraftBukkitSnapshotSubmitHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitSnapshotSubmitHandler.class);
    }

    default IMinecraftBukkitVisualHandler getMinecraftBukkitVisualHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitVisualHandler.class);
    }

    default IMinecraftBukkitWorldActionHandler getMinecraftBukkitWorldActionHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitWorldActionHandler.class);
    }

    default IMinecraftBukkitStructureWorldActionHandler getMinecraftBukkitStructureWorldActionHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitStructureWorldActionHandler.class);
    }

    default IMinecraftBukkitStructureProtectionHandler getMinecraftBukkitStructureProtectionHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitStructureProtectionHandler.class);
    }

    default IMinecraftBukkitResourcePackHandler getMinecraftBukkitResourcePackHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitResourcePackHandler.class);
    }

    default IMinecraftBukkitPopulationWorldActionHandler getMinecraftBukkitPopulationWorldActionHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitPopulationWorldActionHandler.class);
    }

    default MinecraftBukkitServerView getMinecraftBukkitServerView() {
        return DependencyLoaderAccess.requireInstance(MinecraftBukkitServerView.class);
    }

    default IMinecraftBukkitLogger getMinecraftBukkitLogger() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitLogger.class);
    }

    default IKingdomCommandRouter getKingdomCommandRouter() {
        return DependencyLoaderAccess.requireInstance(IKingdomCommandRouter.class);
    }

    default IMinecraftBukkitInventoryUiHandler getKingdomInventoryUiHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitInventoryUiHandler.class);
    }

    default IMinecraftBukkitInteractionMenuHandler getMinecraftBukkitInteractionMenuHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitInteractionMenuHandler.class);
    }
}
