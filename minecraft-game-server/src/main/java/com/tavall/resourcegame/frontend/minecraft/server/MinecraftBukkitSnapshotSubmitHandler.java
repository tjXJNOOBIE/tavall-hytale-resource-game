package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.shared.frontend.MinecraftServerRuntimeSnapshot;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.io.IOException;

public final class MinecraftBukkitSnapshotSubmitHandler implements IMinecraftBukkitSnapshotSubmitHandler, IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    @Override
    public void submitSnapshotQuietly() {
        try {
            MinecraftServerRuntimeSnapshot snapshot = getMinecraftBukkitSnapshotHandler()
                    .createSnapshot(getMinecraftBukkitServerView(), System.currentTimeMillis());
            boolean submitted = getMinecraftBukkitSnapshotClientHandler().submitSnapshot(snapshot);
            if (!submitted) {
                getMinecraftBukkitLogger().warning("Tavall Resource Game server snapshot was rejected. serverId=" + getMinecraftBukkitServerConfig().serverId());
            }
        } catch (IOException exception) {
            getMinecraftBukkitLogger().warning("Failed to submit Tavall Resource Game server snapshot: " + exception.getMessage());
        }
    }
}
