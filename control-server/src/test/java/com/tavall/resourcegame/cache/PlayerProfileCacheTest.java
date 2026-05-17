package com.tavall.resourcegame.cache;

import com.tavall.resourcegame.domain.PlayerProfile;
import com.tavall.resourcegame.player.cache.PlayerProfileCache;
import com.tavall.resourcegame.services.JsonMapperProvider;
import org.junit.jupiter.api.Test;
import org.tavall.abstractcache.semantic.SemanticCacheBuilder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlayerProfileCacheTest {
    @Test
    void readsWritesAndInvalidatesProfilesByPlayerId() {
        PlayerProfileCache cache = new PlayerProfileCache(
                new SemanticCacheBuilder().cacheName("player-profile-test").withHotMemoryTier().build(),
                new JacksonCacheCodec<>(new JsonMapperProvider().mapper(), PlayerProfile.class, "player-profile-test")
        );
        UUID playerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Instant now = Instant.parse("2026-05-16T12:00:00Z");
        PlayerProfile profile = new PlayerProfile(1L, playerId, "Miner", "UTC", "ip-hash", now, now, now);

        cache.write(playerId, profile);

        Optional<PlayerProfile> cached = cache.read(playerId);
        assertTrue(cached.isPresent());
        assertEquals("Miner", cached.get().name());

        assertTrue(cache.invalidate(playerId));
        assertFalse(cache.read(playerId).isPresent());
    }
}
