package com.tavall.resourcegame.shared.frontend;

import java.util.Map;
import java.util.Objects;

public record MinecraftServerRuntimeSnapshotResult(
        String serverId,
        boolean accepted,
        String message,
        long acceptedAtEpochMillis,
        Map<String, String> metadata
) {
    public MinecraftServerRuntimeSnapshotResult {
        Objects.requireNonNull(serverId, "serverId");
        Objects.requireNonNull(message, "message");
        if (serverId.isBlank()) {
            throw new IllegalArgumentException("serverId must not be blank");
        }
        if (message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        if (acceptedAtEpochMillis <= 0) {
            throw new IllegalArgumentException("acceptedAtEpochMillis must be positive");
        }
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
