package com.tavall.resourcegame.middleware.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.api.internal.minecraft.MinecraftServerRuntimeSnapshot;
import com.tavall.resourcegame.api.internal.minecraft.MinecraftServerRuntimeSnapshotResult;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public interface IMinecraftServerSnapshotIngressHandler extends IDependencyInjectableInterface {
    MinecraftServerRuntimeSnapshotResult ingest(MinecraftServerRuntimeSnapshot snapshot, Instant now);

    Optional<MinecraftServerRuntimeSnapshot> latest(String serverId);

    Optional<MinecraftServerRuntimeSnapshot> latestAny();

    Map<String, MinecraftServerRuntimeSnapshot> latestSnapshotsByServerId();
}
