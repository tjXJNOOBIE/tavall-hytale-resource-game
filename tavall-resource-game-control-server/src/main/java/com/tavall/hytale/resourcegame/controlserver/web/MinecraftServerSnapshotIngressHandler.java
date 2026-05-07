package com.tavall.hytale.resourcegame.controlserver.web;

import com.tavall.hytale.resourcegame.shared.frontend.MinecraftServerRuntimeSnapshot;
import com.tavall.hytale.resourcegame.shared.frontend.MinecraftServerRuntimeSnapshotResult;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendSurfaceIdentity;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class MinecraftServerSnapshotIngressHandler {
    private final Map<String, MinecraftServerRuntimeSnapshot> latestSnapshotsByServerId = new ConcurrentHashMap<>();

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
