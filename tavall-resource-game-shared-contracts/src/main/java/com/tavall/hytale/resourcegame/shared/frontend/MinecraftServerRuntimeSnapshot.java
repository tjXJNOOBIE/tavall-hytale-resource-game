package com.tavall.hytale.resourcegame.shared.frontend;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record MinecraftServerRuntimeSnapshot(
        ResourceGameFrontendSurfaceIdentity surfaceIdentity,
        String serverId,
        String proxyId,
        String hostname,
        long startedAtEpochMillis,
        long observedAtEpochMillis,
        int onlinePlayerCount,
        int maxPlayerCount,
        List<MinecraftPlayerRuntimeSnapshot> players,
        Map<String, String> metadata
) {
    public MinecraftServerRuntimeSnapshot {
        Objects.requireNonNull(surfaceIdentity, "surfaceIdentity");
        Objects.requireNonNull(serverId, "serverId");
        Objects.requireNonNull(hostname, "hostname");

        if (serverId.isBlank()) {
            throw new IllegalArgumentException("serverId must not be blank");
        }
        if (hostname.isBlank()) {
            throw new IllegalArgumentException("hostname must not be blank");
        }
        if (startedAtEpochMillis <= 0) {
            throw new IllegalArgumentException("startedAtEpochMillis must be positive");
        }
        if (observedAtEpochMillis <= 0) {
            throw new IllegalArgumentException("observedAtEpochMillis must be positive");
        }
        if (onlinePlayerCount < 0) {
            throw new IllegalArgumentException("onlinePlayerCount must not be negative");
        }
        if (maxPlayerCount < 0) {
            throw new IllegalArgumentException("maxPlayerCount must not be negative");
        }

        players = players == null ? List.of() : List.copyOf(players);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
