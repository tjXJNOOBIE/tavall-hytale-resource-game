package org.tavall.api.minecraft.player;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record PlayerDataResponse(
        String requestId,
        boolean success,
        String message,
        UUID playerId,
        String displayName,
        boolean accountExists,
        boolean accountDisabled,
        String primaryEmail,
        Instant createdAt,
        Instant updatedAt,
        int accountLevel,
        int accountExperience,
        long accountTotalExperience,
        List<PlayerPlatformBindingView> platformBindings,
        Map<String, String> metadata
) {
    public PlayerDataResponse {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(displayName, "displayName");
        if (requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        platformBindings = platformBindings == null ? List.of() : List.copyOf(platformBindings);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        primaryEmail = primaryEmail == null ? "" : primaryEmail;
    }

    public static PlayerDataResponse unavailable(PlayerDataRequest request, String message) {
        return new PlayerDataResponse(
                request == null ? "player-data-unavailable" : request.requestId(),
                false,
                message == null || message.isBlank() ? "Player data is unavailable." : message,
                request == null ? new java.util.UUID(0L, 0L) : request.playerId(),
                request == null ? "Player" : request.playerName(),
                false,
                false,
                "",
                null,
                null,
                0,
                0,
                0L,
                List.of(),
                request == null ? Map.of() : request.context()
        );
    }
}
