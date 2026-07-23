package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.api.minecraft.MinecraftServerRuntimeSnapshot;
import org.tavall.api.minecraft.MinecraftServerRuntimeSnapshotResult;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendSurfaceIdentity;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class MinecraftServerSnapshotIngressHandler implements IMinecraftServerSnapshotIngressHandler, IDependencyInjectableConcrete {
    private final Map<String, MinecraftServerRuntimeSnapshot> latestSnapshotsByServerId = new ConcurrentHashMap<>();

    /**
     * Keeps backend-only Bukkit/Paper world and player observations in the plain Java
     * control plane so Spring remains an HTTP adapter rather than a state owner.
     */
    public MinecraftServerRuntimeSnapshotResult ingest(MinecraftServerRuntimeSnapshot snapshot, Instant now) {
        if (snapshot.surfaceIdentity() != ResourceGameFrontendSurfaceIdentity.BUKKIT_SERVER
                && snapshot.surfaceIdentity() != ResourceGameFrontendSurfaceIdentity.CUSTOM_RUNTIME) {
            return new MinecraftServerRuntimeSnapshotResult(
                    snapshot.serverId(),
                    false,
                    "Unsupported Minecraft server snapshot surface: " + snapshot.surfaceIdentity() + ".",
                    now.toEpochMilli(),
                    Map.of("surfaceIdentity", snapshot.surfaceIdentity().name())
            );
        }

        latestSnapshotsByServerId.put(snapshot.serverId(), snapshot);
        return new MinecraftServerRuntimeSnapshotResult(
                snapshot.serverId(),
                true,
                "Minecraft server snapshot accepted.",
                now.toEpochMilli(),
                Map.of(
                        "surfaceIdentity", snapshot.surfaceIdentity().name(),
                        "onlinePlayerCount", Integer.toString(snapshot.onlinePlayerCount())
                )
        );
    }

    public Optional<MinecraftServerRuntimeSnapshot> latest(String serverId) {
        if (serverId == null || serverId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(latestSnapshotsByServerId.get(serverId));
    }

    public Optional<MinecraftServerRuntimeSnapshot> latestAny() {
        return latestSnapshotsByServerId.values().stream()
                .max(Comparator.comparingLong(MinecraftServerRuntimeSnapshot::observedAtEpochMillis));
    }

    public Map<String, MinecraftServerRuntimeSnapshot> latestSnapshotsByServerId() {
        return new LinkedHashMap<>(latestSnapshotsByServerId);
    }
}
