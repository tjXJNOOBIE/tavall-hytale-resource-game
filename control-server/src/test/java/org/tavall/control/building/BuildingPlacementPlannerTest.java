package org.tavall.control.building;
import org.tavall.control.player.PlayerGameStateHandler;
import org.tavall.control.transport.JsonMapperProvider;
import org.tavall.control.castle.CastleBuildingHandler;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.hypixel.hytale.math.vector.Vector3d;
import org.tavall.control.cache.JacksonCacheCodec;
import org.tavall.control.cache.SemanticCacheFactory;
import org.tavall.control.config.CacheConfig;
import org.tavall.control.domain.BuildingType;
import org.tavall.control.domain.CastleLocationData;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.PlayerProfile;
import org.tavall.control.domain.ResourceInventory;
import org.tavall.control.support.InMemoryPlayerGameStateStore;
import org.tavall.control.support.StubInteriorInstanceHandler;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class BuildingPlacementPlannerTest {
    @Test
    void returnsDeterministicSurfaceAndInteriorAnchorsWhenBuildingsDoNotExist() {
        org.tavall.control.interior.InteriorLayoutHandler layoutHandler = new org.tavall.control.interior.InteriorLayoutHandler();
        PlayerSessionStore sessionStore = new PlayerSessionStore();
        PlayerGameStateHandler gameStateHandler = gameStateHandler("building-placement-planner-state-a");
        StubInteriorInstanceHandler interiorInstanceHandler = new StubInteriorInstanceHandler();
        CastleBuildingHandler buildingHandler = new CastleBuildingHandler(
                sessionStore,
                gameStateHandler,
                interiorInstanceHandler,
                layoutHandler,
                new JsonMapperProvider().mapper()
        );
        BuildingPlacementPlanner planner = new BuildingPlacementPlanner(
                buildingHandler,
                interiorInstanceHandler,
                gameStateHandler,
                layoutHandler
        );

        UUID playerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        PlayerGameState state = seedSession(sessionStore, gameStateHandler, playerId, Instant.parse("2026-04-15T23:00:00Z"));

        assertEquals(interiorInstanceHandler.worldNameFor(playerId), planner.recommendedWorldName(playerId, state, BuildingType.FARMSTEAD));
        Vector3d farmsteadAnchor = layoutHandler.createLayoutForCastle(state.castleLocation()).buildingAnchor(BuildingType.FARMSTEAD);
        assertVector(planner.recommendedPosition(playerId, state, BuildingType.FARMSTEAD), farmsteadAnchor.getX(), farmsteadAnchor.getY(), farmsteadAnchor.getZ());

        assertEquals(interiorInstanceHandler.worldNameFor(playerId), planner.recommendedWorldName(playerId, state, BuildingType.BARRACKS));
        Vector3d barracksAnchor = layoutHandler.createLayoutForCastle(state.castleLocation()).buildingAnchor(BuildingType.BARRACKS);
        assertVector(planner.recommendedPosition(playerId, state, BuildingType.BARRACKS), barracksAnchor.getX(), barracksAnchor.getY(), barracksAnchor.getZ());
    }

    @Test
    void returnsExistingBuildingLocationWhenBuildingAlreadyPlaced() {
        org.tavall.control.interior.InteriorLayoutHandler layoutHandler = new org.tavall.control.interior.InteriorLayoutHandler();
        PlayerSessionStore sessionStore = new PlayerSessionStore();
        PlayerGameStateHandler gameStateHandler = gameStateHandler("building-placement-planner-state-b");
        StubInteriorInstanceHandler interiorInstanceHandler = new StubInteriorInstanceHandler();
        CastleBuildingHandler buildingHandler = new CastleBuildingHandler(
                sessionStore,
                gameStateHandler,
                interiorInstanceHandler,
                layoutHandler,
                new JsonMapperProvider().mapper()
        );
        BuildingPlacementPlanner planner = new BuildingPlacementPlanner(
                buildingHandler,
                interiorInstanceHandler,
                gameStateHandler,
                layoutHandler
        );

        UUID playerId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Instant now = Instant.parse("2026-04-15T23:10:00Z");
        PlayerGameState state = seedSession(sessionStore, gameStateHandler, playerId, now)
                .withResources(new ResourceInventory(200, 200, 200), now);
        state = gameStateHandler.setAccountLevel(state, 50, now);
        sessionStore.get(playerId).updateGameState(state);
        Vector3d farmsteadAnchor = layoutHandler.createLayoutForCastle(state.castleLocation()).buildingAnchor(BuildingType.FARMSTEAD);

        PlayerGameState placedState = buildingHandler.placeBuilding(
                playerId,
                BuildingType.FARMSTEAD,
                interiorInstanceHandler.worldNameFor(playerId),
                farmsteadAnchor,
                now
        ).state();

        assertEquals(interiorInstanceHandler.worldNameFor(playerId), planner.recommendedWorldName(playerId, placedState, BuildingType.FARMSTEAD));
        assertVector(planner.recommendedPosition(playerId, placedState, BuildingType.FARMSTEAD), farmsteadAnchor.getX(), farmsteadAnchor.getY(), farmsteadAnchor.getZ());
    }

    private PlayerGameStateHandler gameStateHandler(String cacheNamespace) {
        JsonMapperProvider mapperProvider = new JsonMapperProvider();
        return new PlayerGameStateHandler(
                new InMemoryPlayerGameStateStore(),
                new SemanticCacheFactory(new CacheConfig("", 6379, "", false)).build(cacheNamespace),
                new JacksonCacheCodec<>(mapperProvider.mapper(), PlayerGameState.class, cacheNamespace),
                mapperProvider.mapper()
        );
    }

    private PlayerGameState seedSession(
            PlayerSessionStore sessionStore,
            PlayerGameStateHandler gameStateHandler,
            UUID playerId,
            Instant now
    ) {
        PlayerGameState initialState = gameStateHandler.loadOrCreate(
                310L,
                playerId,
                new CastleLocationData("overworld", 10.0D, 72.0D, 10.0D),
                now
        );
        sessionStore.put(new PlayerSession(
                playerId,
                new PlayerProfile(310L, playerId, "PlannerBot", "UTC", "hash", now, now, now),
                initialState
        ));
        return initialState;
    }

    private void assertVector(Vector3d actual, double expectedX, double expectedY, double expectedZ) {
        assertEquals(expectedX, actual.getX(), 0.0001D);
        assertEquals(expectedY, actual.getY(), 0.0001D);
        assertEquals(expectedZ, actual.getZ(), 0.0001D);
    }
}

