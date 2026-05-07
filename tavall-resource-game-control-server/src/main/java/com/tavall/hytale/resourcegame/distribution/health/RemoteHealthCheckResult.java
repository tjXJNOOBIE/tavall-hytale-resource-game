package com.tavall.hytale.resourcegame.distribution.health;

import java.time.Instant;
import java.util.Map;

public record RemoteHealthCheckResult(
        String targetId,
        boolean redisReachable,
        boolean postgresReachable,
        boolean hytaleReachable,
        Instant checkedAt,
        Map<String, String> metadata
) {
    public RemoteHealthCheckResult {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean healthy() {
        return redisReachable && postgresReachable && hytaleReachable;
    }
}
