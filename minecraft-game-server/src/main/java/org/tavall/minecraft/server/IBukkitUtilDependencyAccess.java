package org.tavall.minecraft.server;

import org.tavall.minecraft.server.protection.IMinecraftBukkitStructureProtectionHandler;
import org.tavall.minecraft.server.resourcepack.IMinecraftBukkitResourcePackHandler;
import org.tavall.minecraft.server.json.IMinecraftBukkitJsonHandler;
import org.tavall.minecraft.server.json.IMinecraftBukkitJsonMapper;
import org.tavall.minecraft.server.logging.IMinecraftBukkitLogger;
import org.tavall.minecraft.server.snapshot.IMinecraftBukkitSnapshotClientHandler;
import org.tavall.minecraft.server.snapshot.IMinecraftBukkitSnapshotHandler;
import org.tavall.minecraft.server.snapshot.IMinecraftBukkitSnapshotSubmitHandler;
import org.tavall.minecraft.server.tasks.IMinecraftBukkitTaskScheduler;
import org.tavall.minecraft.server.view.MinecraftBukkitServerView;
import org.tavall.minecraft.server.visual.IMinecraftBukkitVisualHandler;
import org.tavall.dependency.DependencyLoaderAccess;

public interface IBukkitUtilDependencyAccess extends MinecraftBukkitServerDomain {
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

    default IMinecraftBukkitResourcePackStatusHandler getMinecraftBukkitResourcePackStatusHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftBukkitResourcePackStatusHandler.class);
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
