package org.tavall.control.cache;

import org.tavall.control.domain.AgingState;
import org.tavall.control.domain.CastleLocationData;
import org.tavall.control.domain.CitizenJobType;
import org.tavall.control.domain.CitizenMetaData;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.PopulationSummary;
import org.tavall.control.domain.ResourceInventory;
import org.tavall.control.domain.TroopMetaData;
import org.tavall.control.config.CacheConfig;
import org.tavall.control.player.cache.PlayerGameStateCache;
import org.tavall.control.transport.JsonMapperProvider;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlayerGameStateCacheTest {
    @Test
    void readsWritesAndInvalidatesGameStateByPlayerId() {
        PlayerGameStateCache cache = PlayerGameStateCache.open(new CacheConfig("", 6379, "", false), new JsonMapperProvider().mapper());
        UUID playerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Instant now = Instant.parse("2026-05-16T12:00:00Z");
        PlayerGameState state = new PlayerGameState(
                7L,
                42L,
                UUID.fromString("79c227d1-40f5-4b88-9f64-bc9b02d766ea"),
                "stone_column_castle",
                new CastleLocationData("default", 10, 64, 10),
                new PopulationSummary(
                        17,
                        3,
                        new CitizenMetaData(0.7, 0.4, 0.8, Map.of(CitizenJobType.GATHERER, 9)),
                        new TroopMetaData(0.6, 0.7, 0.8),
                        new AgingState(now, Duration.ofHours(6))
                ),
                new ResourceInventory(61, 73, 29),
                null,
                "{\"note\":\"round-trip\"}",
                now,
                now
        );

        cache.prime(playerId, state);

        Optional<PlayerGameState> cached = cache.read(playerId);
        assertTrue(cached.isPresent());
        assertEquals("stone_column_castle", cached.get().castleAssetType());

        assertTrue(cache.invalidate(playerId));
        assertFalse(cache.read(playerId).isPresent());
    }
}

