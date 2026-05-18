package org.tavall.control.player;
import org.tavall.control.interior.InteriorTourMarkerService;
import org.tavall.control.population.PopulationDisplayGateway;
import org.tavall.control.player.PlayerGameStateService;
import org.tavall.control.world.WorldLabelService;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import org.tavall.control.dependency.TestKingdomClockService;
import org.tavall.control.domain.AgingState;
import org.tavall.control.domain.CastleLocationData;
import org.tavall.control.domain.CitizenMetaData;
import org.tavall.control.domain.InteriorSessionData;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.PlayerProfile;
import org.tavall.control.domain.PopulationSummary;
import org.tavall.control.domain.ResourceInventory;
import org.tavall.control.domain.TroopMetaData;
import org.tavall.control.support.NoopIpHashService;
import org.tavall.control.support.RecordingCastleBuildingVisualService;
import org.tavall.control.support.RecordingCastleSpawnService;
import org.tavall.control.support.RecordingInteriorInstanceService;
import org.tavall.control.support.RecordingPlayerGameStateService;
import org.tavall.control.support.RecordingPlayerProfileService;
import org.tavall.control.support.RecordingPopulationDisplayGateway;
import org.tavall.control.support.RecordingResourceNodeVisualService;
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

public final class PlayerDataServiceDisconnectTest {
    @Test
    void disconnectClearsInteriorSessionAndAnchors() throws Exception {
        UUID playerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Instant now = Instant.parse("2026-04-22T15:00:00Z");

        PlayerSessionStore sessionStore = new PlayerSessionStore();
        RecordingPlayerProfileService profileService = new RecordingPlayerProfileService();
        RecordingPlayerGameStateService gameStateService = new RecordingPlayerGameStateService();
        RecordingCastleSpawnService castleSpawnService = new RecordingCastleSpawnService();
        RecordingInteriorInstanceService interiorInstanceService = new RecordingInteriorInstanceService();
        NoopIpHashService ipHashService = new NoopIpHashService();
        TestKingdomClockService clockService = new TestKingdomClockService();
        RecordingResourceNodeVisualService nodeVisualService = new RecordingResourceNodeVisualService();
        RecordingCastleBuildingVisualService buildingVisualService = new RecordingCastleBuildingVisualService();
        RecordingPopulationDisplayGateway populationDisplayGateway = new RecordingPopulationDisplayGateway();
        InteriorTourMarkerService tourMarkerService = new InteriorTourMarkerService(new WorldLabelService());
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
        seedTourMarkers(tourMarkerService, playerId);

        PlayerDataService playerDataService = new PlayerDataService(
                profileService,
                gameStateService,
                sessionStore,
                castleSpawnService,
                interiorInstanceService,
                ipHashService,
                clockService,
                nodeVisualService,
                buildingVisualService,
                populationDisplayGateway,
                tourMarkerService,
                uiNavigator
        );

        playerDataService.handlePlayerDisconnect(playerId);

        TestAwait.until(
                () -> gameStateService.persistedState() != null && profileService.persistedProfile() != null,
                Duration.ofSeconds(2),
                "Disconnect persist calls never completed."
        );

        PlayerGameState persistedState = gameStateService.persistedState();
        assertNotNull(persistedState);
        assertNull(persistedState.interiorSession());
        assertNull(populationDisplayGateway.lastSummary(playerId));
        assertNull(sessionStore.get(playerId));
        assertEquals(playerId, interiorInstanceService.releasedPlayerId());
        assertNull(uiNavigator.lastState(playerId));
        assertFalse(hasTourMarkers(tourMarkerService, playerId));
    }

    private void seedTourMarkers(InteriorTourMarkerService service, UUID playerId) throws Exception {
        Field field = InteriorTourMarkerService.class.getDeclaredField("markerRefs");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<UUID, List<?>> markerRefs = (Map<UUID, List<?>>) field.get(service);
        markerRefs.put(playerId, List.of());
    }

    private boolean hasTourMarkers(InteriorTourMarkerService service, UUID playerId) throws Exception {
        Field field = InteriorTourMarkerService.class.getDeclaredField("markerRefs");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<UUID, List<?>> markerRefs = (Map<UUID, List<?>>) field.get(service);
        return markerRefs.containsKey(playerId);
    }
}

