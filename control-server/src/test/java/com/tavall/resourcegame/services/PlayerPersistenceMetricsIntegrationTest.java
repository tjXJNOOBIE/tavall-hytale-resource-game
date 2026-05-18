package org.tavall.control.player;
import org.tavall.control.player.PlayerGameStateService;
import org.tavall.control.transport.JsonMapperProvider;
import org.tavall.control.runtime.InfrastructureMetricsRecorder;

import org.tavall.control.cache.JacksonCacheCodec;
import org.tavall.control.cache.SemanticCacheFactory;
import org.tavall.control.config.CacheConfig;
import org.tavall.control.domain.CastleLocationData;
import org.tavall.control.domain.InfrastructureMetricsSnapshot;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.PlayerProfile;
import org.tavall.control.persistence.InMemoryPlayerGameStateStore;
import org.tavall.control.persistence.InMemoryPlayerProfileStore;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class PlayerPersistenceMetricsIntegrationTest {
    @Test
    void profileAndGameStateServicesRecordCacheRatesAndSaveLatency() {
        JsonMapperProvider mapperProvider = new JsonMapperProvider();
        InfrastructureMetricsRecorder metricsRecorder = InfrastructureMetricsRecorder.isolated();
        SemanticCacheFactory cacheFactory = new SemanticCacheFactory(new CacheConfig("", 6379, "", false));
        PlayerProfileService profileService = new PlayerProfileService(
                new InMemoryPlayerProfileStore(),
                cacheFactory.build("metrics-profile"),
                new JacksonCacheCodec<>(mapperProvider.mapper(), PlayerProfile.class, "metrics-profile"),
                metricsRecorder
        );
        PlayerGameStateService gameStateService = new PlayerGameStateService(
                new InMemoryPlayerGameStateStore(),
                cacheFactory.build("metrics-game-state"),
                new JacksonCacheCodec<>(mapperProvider.mapper(), PlayerGameState.class, "metrics-game-state"),
                mapperProvider.mapper(),
                metricsRecorder
        );

        Instant now = Instant.parse("2026-04-30T16:00:00Z");
        UUID playerId = UUID.randomUUID();
        PlayerProfile profile = profileService.loadOrCreate(playerId, "MetricsPlayer", "UTC", "ipHash", now);
        profileService.loadOrCreate(playerId, "MetricsPlayer", "UTC", "ipHash", now.plusSeconds(5));
        PlayerGameState gameState = gameStateService.loadOrCreate(
                profile.id(),
                playerId,
                new CastleLocationData("metrics-world", 1.0D, 70.0D, 1.0D),
                now
        );
        gameStateService.loadOrCreate(
                profile.id(),
                playerId,
                new CastleLocationData("metrics-world", 1.0D, 70.0D, 1.0D),
                now.plusSeconds(5)
        );
        gameStateService.persistState(gameState, now.plusSeconds(10));

        InfrastructureMetricsSnapshot snapshot = metricsRecorder.snapshot();

        assertEquals(1L, snapshot.profileCacheMisses());
        assertEquals(1L, snapshot.profileCacheHits());
        assertEquals(1L, snapshot.gameStateCacheMisses());
        assertEquals(1L, snapshot.gameStateCacheHits());
        assertEquals(0L, snapshot.cacheReadFailures());
        assertTrue(snapshot.profileSaveCount() >= 1L);
        assertTrue(snapshot.gameStateSaveCount() >= 2L);
        assertTrue(snapshot.averageProfileSaveMillis() >= 0.0D);
        assertTrue(snapshot.averageGameStateSaveMillis() >= 0.0D);
        assertTrue(snapshot.cacheHitRate() > 0.0D);
    }
}

