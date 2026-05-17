package com.tavall.resourcegame.services;

import com.tavall.resourcegame.dependency.TestKingdomClockService;
import com.tavall.resourcegame.domain.AgingState;
import com.tavall.resourcegame.domain.CastleLocationData;
import com.tavall.resourcegame.domain.CitizenMetaData;
import com.tavall.resourcegame.domain.InteriorSessionData;
import com.tavall.resourcegame.domain.PlayerGameState;
import com.tavall.resourcegame.domain.PlayerProfile;
import com.tavall.resourcegame.domain.PopulationSummary;
import com.tavall.resourcegame.domain.ResourceInventory;
import com.tavall.resourcegame.domain.TroopMetaData;
import com.tavall.resourcegame.support.NoopIpHashService;
import com.tavall.resourcegame.support.RecordingCastleBuildingVisualService;
import com.tavall.resourcegame.support.RecordingCastleSpawnService;
import com.tavall.resourcegame.support.RecordingInteriorInstanceService;
import com.tavall.resourcegame.support.RecordingPlayerGameStateService;
import com.tavall.resourcegame.support.RecordingPlayerProfileService;
import com.tavall.resourcegame.support.RecordingPopulationDisplayGateway;
import com.tavall.resourcegame.support.RecordingResourceNodeVisualService;
import com.tavall.resourcegame.support.RecordingUiNavigator;
import com.tavall.resourcegame.support.TestAwait;
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

