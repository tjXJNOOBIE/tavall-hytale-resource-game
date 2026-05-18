package org.tavall.control;
import org.tavall.control.population.PopulationDisplayGateway;

import org.tavall.control.cache.JacksonCacheCodec;
import org.tavall.control.cache.SemanticCacheFactory;
import org.tavall.control.config.CacheConfig;
import org.tavall.control.domain.CastleLocationData;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.PlayerProfile;
import org.tavall.control.population.PromotionCost;
import org.tavall.control.resources.ResourceType;
import org.tavall.control.transport.JsonMapperProvider;
import org.tavall.control.castle.CastleEconomyPlanner;
import org.tavall.control.player.PlayerGameStateHandler;
import org.tavall.control.player.PlayerProfileHandler;
import org.tavall.control.player.PlayerSession;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.population.PopulationHandler;
import org.tavall.control.resource.ResourceNodeHandler;
import org.tavall.control.resource.ResourceHandler;
import org.tavall.control.support.RecordingCastleSiteVisualHandler;
import org.tavall.control.support.InMemoryPlayerGameStateStore;
import org.tavall.control.support.InMemoryPlayerProfileStore;
import org.tavall.control.support.NoopCastleBuildingHandler;
import org.tavall.control.support.RecordingPopulationDisplayGateway;
import org.tavall.control.support.RecordingResourceNodeVisualHandler;
import org.tavall.control.support.RecordingUIData;
import org.tavall.control.support.TestAwait;
import org.junit.jupiter.api.Test;
import org.tavall.abstractcache.semantic.SemanticCache;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class VerticalSliceHandlerTest {
    @Test
    void loadOrCreateUsesCacheAndBuildsStarterState() {
        JsonMapperProvider mapperProvider = new JsonMapperProvider();
        SemanticCacheFactory cacheFactory = new SemanticCacheFactory(new CacheConfig("", 6379, "", false));
        SemanticCache profileCache = cacheFactory.build("profile-test");
        SemanticCache stateCache = cacheFactory.build("state-test");
        InMemoryPlayerProfileStore profileStore = new InMemoryPlayerProfileStore();
        InMemoryPlayerGameStateStore gameStateStore = new InMemoryPlayerGameStateStore();

        PlayerProfileHandler profileHandler = new PlayerProfileHandler(
                profileStore,
                profileCache,
                new JacksonCacheCodec<>(mapperProvider.mapper(), PlayerProfile.class, "profile-test")
        );
        PlayerGameStateHandler gameStateHandler = new PlayerGameStateHandler(
                gameStateStore,
                stateCache,
                new JacksonCacheCodec<>(mapperProvider.mapper(), PlayerGameState.class, "state-test"),
                mapperProvider.mapper()
        );

        UUID playerId = UUID.randomUUID();
        Instant now = Instant.parse("2026-04-08T10:00:00Z");
        CastleLocationData spawn = new CastleLocationData("overworld", 12.0, 64.0, 18.0);

        PlayerProfile firstProfile = profileHandler.loadOrCreate(playerId, "SliceBot", "America/Los_Angeles", "hashed-ip", now);
        PlayerGameState firstState = gameStateHandler.loadOrCreate(firstProfile.id(), playerId, spawn, now);
        PlayerProfile secondProfile = profileHandler.loadOrCreate(playerId, "SliceBot", "America/Los_Angeles", "hashed-ip", now.plusSeconds(30));
        PlayerGameState secondState = gameStateHandler.loadOrCreate(firstProfile.id(), playerId, spawn, now.plusSeconds(30));

        assertEquals(firstProfile.id(), secondProfile.id());
        assertEquals(firstState.castleId(), secondState.castleId());
        assertEquals("overworld", firstState.castleLocation().worldName());
        assertEquals(12, firstState.populationSummary().citizenCount());
        assertEquals(0, firstState.populationSummary().troopCount());
        assertEquals(40, firstState.resources().food());
        assertEquals(25, firstState.resources().wood());
        assertEquals(10, firstState.resources().iron());
        assertTrue(profileStore.findCalls() <= 1, "profile store should not be hit after cache warm");
        assertTrue(gameStateStore.findCalls() <= 1, "game state store should not be hit after cache warm");
    }

    @Test
    void populationAndResourcesFlowPersistsAndUpdatesDisplayState() {
        JsonMapperProvider mapperProvider = new JsonMapperProvider();
        SemanticCacheFactory cacheFactory = new SemanticCacheFactory(new CacheConfig("", 6379, "", false));
        InMemoryPlayerGameStateStore gameStateStore = new InMemoryPlayerGameStateStore();
        PlayerGameStateHandler gameStateHandler = new PlayerGameStateHandler(
                gameStateStore,
                cacheFactory.build("flow-state"),
                new JacksonCacheCodec<>(mapperProvider.mapper(), PlayerGameState.class, "flow-state"),
                mapperProvider.mapper()
        );

        PlayerSessionStore sessionStore = new PlayerSessionStore();
        RecordingCastleSiteVisualHandler castleSiteVisualHandler = new RecordingCastleSiteVisualHandler();
        RecordingResourceNodeVisualHandler resourceNodeVisualHandler = new RecordingResourceNodeVisualHandler();
        RecordingUIData uiNavigator = new RecordingUIData();
        ResourceNodeHandler resourceNodeHandler = new ResourceNodeHandler(sessionStore, gameStateHandler, mapperProvider.mapper(), new CastleEconomyPlanner());
        ResourceHandler resourceHandler = new ResourceHandler(sessionStore, gameStateHandler, castleSiteVisualHandler, uiNavigator);
        RecordingPopulationDisplayGateway displayGateway = new RecordingPopulationDisplayGateway();
        PopulationHandler populationHandler = new PopulationHandler(
                sessionStore,
                gameStateHandler,
                resourceHandler,
                castleSiteVisualHandler,
                displayGateway,
                PromotionCost.defaultCost(),
                new NoopCastleBuildingHandler(),
                resourceNodeHandler,
                resourceNodeVisualHandler,
                uiNavigator
        );

        UUID playerId = UUID.randomUUID();
        Instant now = Instant.parse("2026-04-08T11:00:00Z");
        PlayerGameState initialState = gameStateHandler.loadOrCreate(
                44L,
                playerId,
                new CastleLocationData("overworld", 4.0, 70.0, 4.0),
                now
        );
        PlayerProfile profile = new PlayerProfile(44L, playerId, "FlowBot", "America/Los_Angeles", "ip", now, now, now);
        sessionStore.put(new PlayerSession(playerId, profile, initialState));

        resourceHandler.addResource(playerId, ResourceType.FOOD, 20);
        resourceHandler.setResource(playerId, ResourceType.WOOD, 80);
        resourceHandler.setResource(playerId, ResourceType.IRON, 30);

        assertTrue(populationHandler.promoteCitizen(playerId));
        assertTrue(populationHandler.promoteCitizen(playerId));
        assertTrue(populationHandler.demoteTroop(playerId));
        PlayerGameState finalState = populationHandler.addCitizens(playerId, 3);

        assertNotNull(finalState);
        assertEquals(14, finalState.populationSummary().citizenCount());
        assertEquals(1, finalState.populationSummary().troopCount());
        assertEquals(52, finalState.resources().food());
        assertEquals(76, finalState.resources().wood());
        assertEquals(28, finalState.resources().iron());
        assertEquals(14, displayGateway.lastSummary(playerId).citizenCount());
        assertEquals(1, displayGateway.lastSummary(playerId).troopCount());
        assertEquals(14, castleSiteVisualHandler.lastState(playerId).populationSummary().citizenCount());
        assertTrue(castleSiteVisualHandler.refreshCount(playerId) >= 4, "castle site visuals should refresh on mutations");

        TestAwait.until(
                () -> gameStateStore.snapshot(44L)
                        .map(snapshot -> snapshot.populationSummary().citizenCount() == 14
                                && snapshot.populationSummary().troopCount() == 1
                                && snapshot.resources().food() == 52)
                        .orElse(false),
                Duration.ofSeconds(5),
                "async state persistence did not complete"
        );
    }

    @Test
    void upgradeActionStatesExplainBlockedAndReadyCases() {
        JsonMapperProvider mapperProvider = new JsonMapperProvider();
        SemanticCacheFactory cacheFactory = new SemanticCacheFactory(new CacheConfig("", 6379, "", false));
        InMemoryPlayerGameStateStore gameStateStore = new InMemoryPlayerGameStateStore();
        PlayerGameStateHandler gameStateHandler = new PlayerGameStateHandler(
                gameStateStore,
                cacheFactory.build("ui-state"),
                new JacksonCacheCodec<>(mapperProvider.mapper(), PlayerGameState.class, "ui-state"),
                mapperProvider.mapper()
        );

        PlayerSessionStore sessionStore = new PlayerSessionStore();
        RecordingCastleSiteVisualHandler castleSiteVisualHandler = new RecordingCastleSiteVisualHandler();
        RecordingResourceNodeVisualHandler resourceNodeVisualHandler = new RecordingResourceNodeVisualHandler();
        RecordingUIData uiNavigator = new RecordingUIData();
        ResourceNodeHandler resourceNodeHandler = new ResourceNodeHandler(sessionStore, gameStateHandler, mapperProvider.mapper(), new CastleEconomyPlanner());
        ResourceHandler resourceHandler = new ResourceHandler(sessionStore, gameStateHandler, castleSiteVisualHandler, uiNavigator);
        PopulationHandler populationHandler = new PopulationHandler(
                sessionStore,
                gameStateHandler,
                resourceHandler,
                castleSiteVisualHandler,
                new RecordingPopulationDisplayGateway(),
                PromotionCost.defaultCost(),
                new NoopCastleBuildingHandler(),
                resourceNodeHandler,
                resourceNodeVisualHandler,
                uiNavigator
        );

        UUID playerId = UUID.randomUUID();
        Instant now = Instant.parse("2026-04-08T11:30:00Z");
        PlayerGameState initialState = gameStateHandler.loadOrCreate(
                77L,
                playerId,
                new CastleLocationData("overworld", 1.0, 65.0, 1.0),
                now
        );
        PlayerProfile profile = new PlayerProfile(77L, playerId, "UiBot", "UTC", "ip", now, now, now);
        sessionStore.put(new PlayerSession(playerId, profile, initialState));

        PlayerGameState noCitizens = populationHandler.setCitizens(playerId, 0);
        assertFalse(populationHandler.promoteActionState(noCitizens).allowed());
        assertEquals("Blocked: need at least 1 citizen.", populationHandler.promoteActionState(noCitizens).message());
        assertFalse(populationHandler.demoteActionState(noCitizens).allowed());
        assertEquals("Blocked: need at least 1 troop.", populationHandler.demoteActionState(noCitizens).message());

        resourceHandler.setResource(playerId, ResourceType.FOOD, 4);
        resourceHandler.setResource(playerId, ResourceType.WOOD, 0);
        resourceHandler.setResource(playerId, ResourceType.IRON, 1);
        PlayerGameState woodBlocked = populationHandler.setCitizens(playerId, 2);
        assertFalse(populationHandler.promoteActionState(woodBlocked).allowed());
        assertEquals("Blocked: need 2 Wood.", populationHandler.promoteActionState(woodBlocked).message());

        resourceHandler.setResource(playerId, ResourceType.WOOD, 2);
        PlayerGameState readyState = sessionStore.get(playerId).gameState();
        assertTrue(populationHandler.promoteActionState(readyState).allowed());
        assertEquals("Ready: promote 1 citizen into 1 troop.", populationHandler.promoteActionState(readyState).message());
        assertEquals("Cost per promotion: 4 Food, 2 Wood, 1 Iron.", populationHandler.promotionCostSummary(readyState));
    }
}
