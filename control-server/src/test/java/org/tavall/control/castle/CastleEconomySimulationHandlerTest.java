package org.tavall.control.castle;
import org.tavall.control.castle.CastleEconomyPlanner;
import org.tavall.control.player.PlayerGameStateHandler;
import org.tavall.control.transport.JsonMapperProvider;
import org.tavall.control.resource.ResourceNodeHandler;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import org.tavall.control.cache.JacksonCacheCodec;
import org.tavall.control.cache.SemanticCacheFactory;
import org.tavall.control.config.CacheConfig;
import org.tavall.control.domain.BuildingType;
import org.tavall.control.domain.CastleLocationData;
import org.tavall.control.domain.CitizenJobType;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.PlayerProfile;
import org.tavall.control.domain.ResourceInventory;
import org.tavall.control.resources.ResourceType;
import org.tavall.control.support.InMemoryPlayerGameStateStore;
import org.tavall.control.support.NoopCastleBuildingHandler;
import org.tavall.control.support.RecordingCastleBuildingVisualHandler;
import org.tavall.control.support.RecordingCastleSiteVisualHandler;
import org.tavall.control.support.RecordingResourceNodeVisualHandler;
import org.tavall.control.support.RecordingUIData;
import org.tavall.control.support.StubInteriorInstanceHandler;
import org.tavall.control.support.TestAwait;
import com.hypixel.hytale.math.vector.Vector3d;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CastleEconomySimulationHandlerTest {
    @Test
    void runTickAddsResourcesAndPersistsJobCounts() {
        JsonMapperProvider mapperProvider = new JsonMapperProvider();
        InMemoryPlayerGameStateStore gameStateStore = new InMemoryPlayerGameStateStore();
        PlayerGameStateHandler gameStateHandler = new PlayerGameStateHandler(
                gameStateStore,
                new SemanticCacheFactory(new CacheConfig("", 6379, "", false)).build("economy-sim"),
                new JacksonCacheCodec<>(mapperProvider.mapper(), PlayerGameState.class, "economy-sim"),
                mapperProvider.mapper()
        );
        PlayerSessionStore sessionStore = new PlayerSessionStore();
        RecordingCastleSiteVisualHandler visualHandler = new RecordingCastleSiteVisualHandler();
        RecordingCastleBuildingVisualHandler buildingVisualHandler = new RecordingCastleBuildingVisualHandler();
        RecordingResourceNodeVisualHandler resourceNodeVisualHandler = new RecordingResourceNodeVisualHandler();
        RecordingUIData uiNavigator = new RecordingUIData();
        CastleEconomyPlanner planner = new CastleEconomyPlanner();
        ResourceNodeHandler resourceNodeHandler = new ResourceNodeHandler(sessionStore, gameStateHandler, mapperProvider.mapper(), new CastleEconomyPlanner());
        CastleEconomySimulationHandler simulationHandler = new CastleEconomySimulationHandler(
                sessionStore,
                gameStateHandler,
                new NoopCastleBuildingHandler(),
                buildingVisualHandler,
                visualHandler,
                planner,
                resourceNodeHandler,
                resourceNodeVisualHandler,
                uiNavigator
        );

        UUID playerId = UUID.randomUUID();
        Instant start = Instant.parse("2026-04-14T18:10:00Z");
        PlayerGameState initialState = gameStateHandler.loadOrCreate(
                91L,
                playerId,
                new CastleLocationData("overworld", 6.0, 72.0, 6.0),
                start
        );
        sessionStore.put(new PlayerSession(
                playerId,
                new PlayerProfile(91L, playerId, "EconomyBot", "UTC", "hash", start, start, start),
                initialState
        ));
        PlayerGameState withNode = resourceNodeHandler.placeNode(
                playerId,
                ResourceType.FOOD,
                "overworld",
                new Vector3d(initialState.castleLocation().x() + 8.0, initialState.castleLocation().y(), initialState.castleLocation().z() + 8.0),
                start
        );
        sessionStore.get(playerId).updateGameState(withNode);

        simulationHandler.runTick(start.plusSeconds(12));

        PlayerGameState updated = sessionStore.get(playerId).gameState();
        assertTrue(updated.resources().food() >= initialState.resources().food());
        assertTrue(updated.resources().wood() >= initialState.resources().wood());
        assertTrue(updated.resources().iron() >= initialState.resources().iron());
        assertTrue(updated.populationSummary().citizenMetaData().jobCounts().containsKey(CitizenJobType.GATHERER));
        assertEquals(updated.populationSummary().citizenCount(), planner.snapshot(updated).jobCounts().values().stream().mapToInt(Integer::intValue).sum());
        assertEquals(updated.populationSummary().citizenCount(), visualHandler.lastState(playerId).populationSummary().citizenCount());
        assertEquals(updated.resources().food(), buildingVisualHandler.lastState(playerId).resources().food());
        assertEquals(updated.resources().food(), resourceNodeVisualHandler.lastState(playerId).resources().food());
        assertEquals(updated.resources().food(), uiNavigator.lastState(playerId).resources().food());

        TestAwait.until(
                () -> gameStateStore.snapshot(91L)
                        .map(snapshot -> snapshot.populationSummary().citizenMetaData().jobCounts().containsKey(CitizenJobType.GATHERER))
                        .orElse(false),
                Duration.ofSeconds(2),
                "economy tick should persist updated job counts"
        );
    }

    @Test
    void runTickCompletesBuildingsAndAppliesTheirBonusYield() {
        JsonMapperProvider mapperProvider = new JsonMapperProvider();
        InMemoryPlayerGameStateStore gameStateStore = new InMemoryPlayerGameStateStore();
        PlayerGameStateHandler gameStateHandler = new PlayerGameStateHandler(
                gameStateStore,
                new SemanticCacheFactory(new CacheConfig("", 6379, "", false)).build("economy-building-sim"),
                new JacksonCacheCodec<>(mapperProvider.mapper(), PlayerGameState.class, "economy-building-sim"),
                mapperProvider.mapper()
        );
        PlayerSessionStore sessionStore = new PlayerSessionStore();
        RecordingCastleSiteVisualHandler siteVisualHandler = new RecordingCastleSiteVisualHandler();
        RecordingCastleBuildingVisualHandler buildingVisualHandler = new RecordingCastleBuildingVisualHandler();
        RecordingResourceNodeVisualHandler resourceNodeVisualHandler = new RecordingResourceNodeVisualHandler();
        RecordingUIData uiNavigator = new RecordingUIData();
        CastleEconomyPlanner planner = new CastleEconomyPlanner();
        CastleBuildingHandler buildingHandler = new CastleBuildingHandler(
                sessionStore,
                gameStateHandler,
                new StubInteriorInstanceHandler(),
                new org.tavall.minecraft.domain.interior.InteriorLayoutHandler(),
                mapperProvider.mapper()
        );
        ResourceNodeHandler resourceNodeHandler = new ResourceNodeHandler(sessionStore, gameStateHandler, mapperProvider.mapper(), new CastleEconomyPlanner());
        CastleEconomySimulationHandler simulationHandler = new CastleEconomySimulationHandler(
                sessionStore,
                gameStateHandler,
                buildingHandler,
                buildingVisualHandler,
                siteVisualHandler,
                planner,
                resourceNodeHandler,
                resourceNodeVisualHandler,
                uiNavigator
        );

        UUID playerId = UUID.randomUUID();
        Instant start = Instant.parse("2026-04-15T00:00:00Z");
        PlayerGameState seededState = gameStateHandler.loadOrCreate(
                111L,
                playerId,
                new CastleLocationData("overworld", 6.0, 72.0, 6.0),
                start
        ).withResources(new ResourceInventory(200, 200, 200), start);
        seededState = gameStateHandler.setAccountLevel(seededState, 50, start);
        sessionStore.put(new PlayerSession(
                playerId,
                new PlayerProfile(111L, playerId, "EconomyBuilderBot", "UTC", "hash", start, start, start),
                seededState
        ));

        PlayerGameState placedState = buildingHandler.placeBuilding(
                playerId,
                BuildingType.FARMSTEAD,
                new StubInteriorInstanceHandler().worldNameFor(playerId),
                new org.tavall.minecraft.domain.interior.InteriorLayoutHandler()
                        .createLayoutForCastle(seededState.castleLocation())
                        .buildingAnchor(BuildingType.FARMSTEAD),
                start
        ).state();
        int expectedFoodAfterTick = placedState.resources().food() + planner.snapshot(placedState).gainFor(ResourceType.FOOD) + 2;

        simulationHandler.runTick(start.plusSeconds(CastleEconomySimulationHandler.TICK_INTERVAL_SECONDS * 2));

        PlayerGameState updated = sessionStore.get(playerId).gameState();
        assertEquals(expectedFoodAfterTick, updated.resources().food());
        assertEquals(1, buildingHandler.listBuildings(updated).getFirst().currentLevel());
        assertTrue(buildingVisualHandler.refreshCount(playerId) >= 1);
        assertEquals(updated.resources().food(), uiNavigator.lastState(playerId).resources().food());
        assertEquals(updated.resources().food(), siteVisualHandler.lastState(playerId).resources().food());
    }
}
