package org.tavall.control.player;
import org.tavall.control.interior.InteriorTourMarkerHandler;
import org.tavall.control.population.PopulationDisplayGateway;
import org.tavall.control.player.PlayerGameStateHandler;
import org.tavall.control.world.WorldLabelHandler;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import org.tavall.control.dependency.TestKingdomClockHandler;
import org.tavall.control.domain.AgingState;
import org.tavall.control.domain.CastleLocationData;
import org.tavall.control.domain.CitizenMetaData;
import org.tavall.control.domain.InteriorSessionData;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.PlayerProfile;
import org.tavall.control.domain.PopulationSummary;
import org.tavall.control.domain.ResourceInventory;
import org.tavall.control.domain.TroopMetaData;
import org.tavall.control.support.NoopIpHashHandler;
import org.tavall.control.support.RecordingCastleBuildingVisualHandler;
import org.tavall.control.support.RecordingCastleSpawnHandler;
import org.tavall.control.support.RecordingInteriorInstanceHandler;
import org.tavall.control.support.RecordingPlayerGameStateHandler;
import org.tavall.control.support.RecordingPlayerProfileHandler;
import org.tavall.control.support.RecordingPopulationDisplayGateway;
import org.tavall.control.support.RecordingResourceNodeVisualHandler;
import org.tavall.control.support.RecordingUiNavigator;
import org.tavall.control.support.TestAwait;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public final class PlayerDataHandlerDisconnectTest {
    @Test
    void disconnectClearsInteriorSessionAndAnchors() throws Exception {
        UUID playerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Instant now = Instant.parse("2026-04-22T15:00:00Z");

        PlayerSessionStore sessionStore = new PlayerSessionStore();
        RecordingPlayerProfileHandler profileHandler = new RecordingPlayerProfileHandler();
        RecordingPlayerGameStateHandler gameStateHandler = new RecordingPlayerGameStateHandler();
        RecordingCastleSpawnHandler castleSpawnHandler = new RecordingCastleSpawnHandler();
        RecordingInteriorInstanceHandler interiorInstanceHandler = new RecordingInteriorInstanceHandler();
        NoopIpHashHandler ipHashHandler = new NoopIpHashHandler();
        TestKingdomClockHandler clockHandler = new TestKingdomClockHandler();
        RecordingResourceNodeVisualHandler nodeVisualHandler = new RecordingResourceNodeVisualHandler();
        RecordingCastleBuildingVisualHandler buildingVisualHandler = new RecordingCastleBuildingVisualHandler();
        RecordingPopulationDisplayGateway populationDisplayGateway = new RecordingPopulationDisplayGateway();
        InteriorTourMarkerHandler tourMarkerHandler = new InteriorTourMarkerHandler(new WorldLabelHandler());
        RecordingUiNavigator uiNavigator = new RecordingUiNavigator();

        CastleLocationData castleLocation = new CastleLocationData("surface-world", 0.5D, 64.0D, 0.5D);
        PopulationSummary populationSummary = new PopulationSummary(
                5,
                2,
                CitizenMetaData.defaults(),
                TroopMetaData.defaults(),
                AgingState.defaults(now)
        );
        InteriorSessionData interiorSession = new InteriorSessionData("kingdom-interiors", castleLocation, now);
        PlayerGameState gameState = new PlayerGameState(
                1L,
                1L,
                UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"),
                "",
                castleLocation,
                populationSummary,
                ResourceInventory.starterPack(),
                interiorSession,
                "{}",
                now,
                now
        );
        PlayerProfile profile = new PlayerProfile(1L, playerId, "DisconnectTester", "UTC", "", now, now, now);
        sessionStore.put(new PlayerSession(playerId, profile, gameState));

        populationDisplayGateway.updateDisplays(playerId, populationSummary);
        uiNavigator.refreshTrackedPage(playerId, gameState);
        seedTourMarkers(tourMarkerHandler, playerId);

        PlayerDataHandler playerDataHandler = new PlayerDataHandler(
                profileHandler,
                gameStateHandler,
                sessionStore,
                castleSpawnHandler,
                interiorInstanceHandler,
                ipHashHandler,
                clockHandler,
                nodeVisualHandler,
                buildingVisualHandler,
                populationDisplayGateway,
                tourMarkerHandler,
                uiNavigator
        );

        playerDataHandler.handlePlayerDisconnect(playerId);

        TestAwait.until(
                () -> gameStateHandler.persistedState() != null && profileHandler.persistedProfile() != null,
                Duration.ofSeconds(2),
                "Disconnect persist calls never completed."
        );

        PlayerGameState persistedState = gameStateHandler.persistedState();
        assertNotNull(persistedState);
        assertNull(persistedState.interiorSession());
        assertNull(populationDisplayGateway.lastSummary(playerId));
        assertNull(sessionStore.get(playerId));
        assertEquals(playerId, interiorInstanceHandler.releasedPlayerId());
        assertNull(uiNavigator.lastState(playerId));
        assertFalse(tourMarkerSnapshot(tourMarkerHandler).containsKey(playerId));
    }

    private static void seedTourMarkers(InteriorTourMarkerHandler tourMarkerHandler, UUID playerId) throws Exception {
        Field markersField = InteriorTourMarkerHandler.class.getDeclaredField("markerRefs");
        markersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<UUID, List<?>> markers = (Map<UUID, List<?>>) markersField.get(tourMarkerHandler);
        markers.put(playerId, List.of());
    }

    private static Map<UUID, List<?>> tourMarkerSnapshot(InteriorTourMarkerHandler tourMarkerHandler) throws Exception {
        Field markersField = InteriorTourMarkerHandler.class.getDeclaredField("markerRefs");
        markersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<UUID, List<?>> markers = (Map<UUID, List<?>>) markersField.get(tourMarkerHandler);
        return markers;
    }
}
