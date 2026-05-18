package org.tavall.control.castle;
import org.tavall.control.player.PlayerGameStateService;
import org.tavall.control.transport.JsonMapperProvider;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import org.tavall.control.cache.JacksonCacheCodec;
import org.tavall.control.cache.SemanticCacheFactory;
import org.tavall.control.config.CacheConfig;
import org.tavall.control.domain.CastleLocationData;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.PlayerProfile;
import org.tavall.control.support.InMemoryPlayerGameStateStore;
import org.tavall.control.support.RecordingCastleBuildingVisualService;
import org.tavall.control.support.RecordingCastleSiteVisualService;
import org.tavall.control.support.RecordingCastleSpawnService;
import org.tavall.control.support.RecordingResourceNodeVisualService;
import org.tavall.control.support.TestAwait;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public final class CastlePlacementServiceTest {
    @Test
    void placeCastleRewritesSessionRefreshesVisualsAndPersists() {
        JsonMapperProvider mapperProvider = new JsonMapperProvider();
        InMemoryPlayerGameStateStore gameStateStore = new InMemoryPlayerGameStateStore();
        PlayerGameStateService gameStateService = new PlayerGameStateService(
                gameStateStore,
                new SemanticCacheFactory(new CacheConfig("", 6379, "", false)).build("castle-placement-state"),
                new JacksonCacheCodec<>(mapperProvider.mapper(), PlayerGameState.class, "castle-placement-state"),
                mapperProvider.mapper()
        );
        PlayerSessionStore sessionStore = new PlayerSessionStore();
        RecordingCastleSpawnService castleSpawnService = new RecordingCastleSpawnService();
        RecordingCastleSiteVisualService castleSiteVisualService = new RecordingCastleSiteVisualService();
        RecordingCastleBuildingVisualService buildingVisualService = new RecordingCastleBuildingVisualService();
        RecordingResourceNodeVisualService resourceNodeVisualService = new RecordingResourceNodeVisualService();
        CastlePlacementService placementService = new CastlePlacementService(
                sessionStore,
                gameStateService,
                castleSpawnService,
                castleSiteVisualService,
                buildingVisualService,
                resourceNodeVisualService
        );

        UUID playerId = UUID.randomUUID();
        Instant start = Instant.parse("2026-04-15T20:00:00Z");
        PlayerGameState initialState = gameStateService.loadOrCreate(
                31L,
                playerId,
                new CastleLocationData("default", 4.0, 65.0, 4.0),
                start
        );
        sessionStore.put(new PlayerSession(
                playerId,
                new PlayerProfile(31L, playerId, "CastleMoveBot", "UTC", "hash", start, start, start),
                initialState
        ));

        CastleLocationData newLocation = new CastleLocationData("default", 18.5, 70.0, -2.5);
        PlayerGameState updatedState = placementService.placeCastle(playerId, newLocation, start.plusSeconds(15));

        assertNotNull(updatedState);
        assertEquals(newLocation.x(), updatedState.castleLocation().x());
        assertEquals(newLocation.y(), updatedState.castleLocation().y());
        assertEquals(newLocation.z(), updatedState.castleLocation().z());
        assertEquals(newLocation, castleSpawnService.replacedLocation(playerId));
        assertEquals(newLocation, sessionStore.get(playerId).gameState().castleLocation());
        assertEquals(newLocation, castleSiteVisualService.lastState(playerId).castleLocation());
        assertEquals(newLocation, buildingVisualService.lastState(playerId).castleLocation());
        assertEquals(newLocation, resourceNodeVisualService.lastState(playerId).castleLocation());

        TestAwait.until(
                () -> gameStateStore.snapshot(31L)
                        .map(snapshot -> snapshot.castleLocation() != null
                                && snapshot.castleLocation().x() == newLocation.x()
                                && snapshot.castleLocation().y() == newLocation.y()
                                && snapshot.castleLocation().z() == newLocation.z())
                        .orElse(false),
                Duration.ofSeconds(2),
                "castle relocation did not persist"
        );
    }
}

