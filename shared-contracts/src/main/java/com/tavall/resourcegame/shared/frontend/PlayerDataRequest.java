package com.tavall.resourcegame.shared.frontend;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record PlayerDataRequest(
        String requestId,
        UUID playerId,
        String playerName,
        String serverId,
        String worldName,
        Map<String, String> context,
        long createdAtEpochMillis
) {
    public PlayerDataRequest {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(playerName, "playerName");
        Objects.requireNonNull(serverId, "serverId");
        if (requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        if (playerName.isBlank()) {
            throw new IllegalArgumentException("playerName must not be blank");
        }
        if (serverId.isBlank()) {
            throw new IllegalArgumentException("serverId must not be blank");
        }
        context = context == null ? Map.of() : Map.copyOf(context);
    }
}
