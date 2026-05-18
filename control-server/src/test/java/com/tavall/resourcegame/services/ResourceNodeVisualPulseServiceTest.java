package org.tavall.control.resource;
import org.tavall.control.player.PlayerGameStateService;
import org.tavall.control.transport.JsonMapperProvider;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import org.tavall.control.domain.CastleLocationData;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.PlayerProfile;
import org.tavall.control.cache.JacksonCacheCodec;
import org.tavall.control.cache.SemanticCacheFactory;
import org.tavall.control.config.CacheConfig;
import org.tavall.control.support.InMemoryPlayerGameStateStore;
import org.tavall.control.support.RecordingResourceNodeVisualService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ResourceNodeVisualPulseServiceTest {
    @Test
    void runPulseRefreshesNodeVisualsForActiveSessions() {
        PlayerSessionStore sessionStore = new PlayerSessionStore();
        RecordingResourceNodeVisualService visualService = new RecordingResourceNodeVisualService();
        ResourceNodeVisualPulseService pulseService = new ResourceNodeVisualPulseService(sessionStore, visualService);
        JsonMapperProvider mapperProvider = new JsonMapperProvider();
        PlayerGameStateService gameStateService = new PlayerGameStateService(
                new InMemoryPlayerGameStateStore(),
                new SemanticCacheFactory(new CacheConfig("", 6379, "", false)).build("node-pulse"),
                new JacksonCacheCodec<>(mapperProvider.mapper(), PlayerGameState.class, "node-pulse"),
                mapperProvider.mapper()
        );

        UUID playerId = UUID.randomUUID();
        Instant now = Instant.parse("2026-04-15T17:05:00Z");
        PlayerProfile profile = new PlayerProfile(1L, playerId, "PulseBot", "UTC", "hash", now, now, now);
        PlayerGameState state = gameStateService.loadOrCreate(1L, playerId, new CastleLocationData("default", 0.0, 72.0, 0.0), now);
        sessionStore.put(new PlayerSession(playerId, profile, state));

        pulseService.runPulse();

        assertEquals(1, visualService.refreshCount(playerId));
        assertEquals(state, visualService.lastState(playerId));
    }
}

