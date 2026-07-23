package org.tavall.minecraft.server.snapshot;

import org.tavall.api.minecraft.MinecraftServerRuntimeSnapshot;
import org.tavall.minecraft.server.MinecraftBukkitServerDomain;
import org.tavall.dependency.IDependencyInjectableConcrete;

import java.io.IOException;

public final class MinecraftBukkitSnapshotSubmitHandler implements IMinecraftBukkitSnapshotSubmitHandler, MinecraftBukkitServerDomain, IDependencyInjectableConcrete {
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
