package org.tavall.control.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.api.minecraft.MinecraftServerRuntimeSnapshot;
import org.tavall.api.minecraft.MinecraftServerRuntimeSnapshotResult;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public interface IMinecraftServerSnapshotIngressHandler extends IDependencyInjectableInterface {
    MinecraftServerRuntimeSnapshotResult ingest(MinecraftServerRuntimeSnapshot snapshot, Instant now);

    Optional<MinecraftServerRuntimeSnapshot> latest(String serverId);

    Optional<MinecraftServerRuntimeSnapshot> latestAny();

    Map<String, MinecraftServerRuntimeSnapshot> latestSnapshotsByServerId();
}
