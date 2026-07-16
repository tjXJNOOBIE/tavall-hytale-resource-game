package org.tavall.api.minecraft.player;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record PlayerPlatformBindingView(
        String platform,
        String platformAccountId,
        String platformDisplayName,
        boolean verified,
        Instant linkedAt,
        Instant lastSeenAt,
        Map<String, String> metadata
) {
    public PlayerPlatformBindingView {
        Objects.requireNonNull(platform, "platform");
        Objects.requireNonNull(platformAccountId, "platformAccountId");
        Objects.requireNonNull(platformDisplayName, "platformDisplayName");
        if (platform.isBlank()) {
            throw new IllegalArgumentException("platform must not be blank");
        }
        if (platformAccountId.isBlank()) {
            throw new IllegalArgumentException("platformAccountId must not be blank");
        }
        if (platformDisplayName.isBlank()) {
            throw new IllegalArgumentException("platformDisplayName must not be blank");
        }
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
