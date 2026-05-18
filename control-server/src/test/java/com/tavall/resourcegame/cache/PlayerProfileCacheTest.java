package org.tavall.control.cache;

import org.tavall.control.config.CacheConfig;
import org.tavall.control.domain.PlayerProfile;
import org.tavall.control.player.cache.PlayerProfileCache;
import org.tavall.control.services.JsonMapperProvider;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlayerProfileCacheTest {
    @Test
    void readsWritesAndInvalidatesProfilesByPlayerId() {
        PlayerProfileCache cache = PlayerProfileCache.open(new CacheConfig("", 6379, "", false), new JsonMapperProvider().mapper());
        UUID playerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Instant now = Instant.parse("2026-05-16T12:00:00Z");
        PlayerProfile profile = new PlayerProfile(1L, playerId, "Miner", "UTC", "ip-hash", now, now, now);

        cache.prime(playerId, profile);

        Optional<PlayerProfile> cached = cache.read(playerId);
        assertTrue(cached.isPresent());
        assertEquals("Miner", cached.get().name());

        assertTrue(cache.invalidate(playerId));
        assertFalse(cache.read(playerId).isPresent());
    }
}
