package com.tavall.resourcegame.api.internal.minecraft;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record MinecraftPlayerRuntimeSnapshot(
        UUID playerId,
        String playerName,
        String worldName,
        double x,
        double y,
        double z,
        float yaw,
        float pitch,
        double health,
        int foodLevel,
        String gameMode,
        boolean online,
        Map<String, String> metadata
) {
    public MinecraftPlayerRuntimeSnapshot {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(playerName, "playerName");
        Objects.requireNonNull(worldName, "worldName");
        Objects.requireNonNull(gameMode, "gameMode");

        if (playerName.isBlank()) {
            throw new IllegalArgumentException("playerName must not be blank");
        }
        if (worldName.isBlank()) {
            throw new IllegalArgumentException("worldName must not be blank");
        }
        if (gameMode.isBlank()) {
            throw new IllegalArgumentException("gameMode must not be blank");
        }

        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
