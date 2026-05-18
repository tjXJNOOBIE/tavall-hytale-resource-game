package org.tavall.control.castle;
import org.tavall.control.player.PlayerGameStateHandler;
import org.tavall.control.transport.JsonMapperProvider;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.hypixel.hytale.math.vector.Vector3d;
import org.tavall.control.cache.JacksonCacheCodec;
import org.tavall.control.cache.SemanticCacheFactory;
import org.tavall.control.config.CacheConfig;
import org.tavall.control.domain.BuildingMutationResult;
import org.tavall.control.domain.BuildingType;
import org.tavall.control.domain.CastleBuildingData;
import org.tavall.control.domain.CastleLocationData;
import org.tavall.control.domain.DebugModeState;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.PlayerProfile;
import org.tavall.control.domain.ResourceInventory;
import org.tavall.control.population.PromotionCost;
import org.tavall.control.support.InMemoryPlayerGameStateStore;
import org.tavall.control.support.StubInteriorInstanceHandler;
import org.tavall.control.support.TestAwait;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CastleBuildingHandlerTest {
    @Test
    void placeBuildingPersistsConstructionAndCompletesAfterTicks() {
        JsonMapperProvider mapperProvider = new JsonMapperProvider();
        InMemoryPlayerGameStateStore gameStateStore = new InMemoryPlayerGameStateStore();
        PlayerGameStateHandler gameStateHandler = new PlayerGameStateHandler(
                gameStateStore,
                new SemanticCacheFactory(new CacheConfig("", 6379, "", false)).build("building-state"),
                new JacksonCacheCodec<>(mapperProvider.mapper(), PlayerGameState.class, "building-state"),
                mapperProvider.mapper()
        );
        PlayerSessionStore sessionStore = new PlayerSessionStore();
        CastleBuildingHandler buildingHandler = new CastleBuildingHandler(
                sessionStore,
                gameStateHandler,
                new StubInteriorInstanceHandler(),
                new org.tavall.control.interior.InteriorLayoutHandler(),
                mapperProvider.mapper()
        );

        UUID playerId = UUID.randomUUID();
        Instant start = Instant.parse("2026-04-15T22:00:00Z");
        PlayerGameState initialState = gameStateHandler.loadOrCreate(
                201L,
                playerId,
                new CastleLocationData("overworld", 10.0, 72.0, 10.0),
                start
        );
        initialState = gameStateHandler.setAccountLevel(initialState, 50, start);
        sessionStore.put(new PlayerSession(
                playerId,
                new PlayerProfile(201L, playerId, "BuilderBot", "UTC", "hash", start, start, start),
                initialState
        ));

        String interiorWorld = new StubInteriorInstanceHandler().worldNameFor(playerId);
        Vector3d buildPosition = new org.tavall.control.interior.InteriorLayoutHandler()
                .createLayoutForCastle(initialState.castleLocation())
                .buildingAnchor(BuildingType.FARMSTEAD);
        BuildingMutationResult placement = buildingHandler.placeBuilding(playerId, BuildingType.FARMSTEAD, interiorWorld, buildPosition, start);

        assertTrue(placement.changed());
        assertEquals("Farmstead construction started.", placement.message());
        assertNotNull(placement.state());
        CastleBuildingData placed = buildingHandler.listBuildings(placement.state()).getFirst();
        assertTrue(placed.isUnderConstruction());
        assertEquals(0, placed.currentLevel());
        assertEquals(1, placed.targetLevel());
        assertEquals(12, placement.state().resources().food());
        assertEquals(13, placement.state().resources().wood());
        assertEquals(10, placement.state().resources().iron());

        PlayerGameState midTickState = buildingHandler.applyTick(playerId, placement.state(), start.plusSeconds(12));
        CastleBuildingData midTickBuilding = buildingHandler.listBuildings(midTickState).getFirst();
        assertTrue(midTickBuilding.isUnderConstruction());

        PlayerGameState completedState = buildingHandler.applyTick(playerId, midTickState, start.plusSeconds(24));
        CastleBuildingData completedBuilding = buildingHandler.listBuildings(completedState).getFirst();
        assertFalse(completedBuilding.isUnderConstruction());
        assertEquals(1, completedBuilding.currentLevel());
        assertEquals(14, completedState.resources().food());
        assertEquals(13, completedState.resources().wood());
        assertEquals(10, completedState.resources().iron());
        assertTrue(completedState.metadataJson().contains(completedBuilding.buildingId().toString()));

        TestAwait.until(
                () -> gameStateStore.snapshot(201L)
                        .map(snapshot -> snapshot.metadataJson() != null && snapshot.metadataJson().contains(completedBuilding.buildingId().toString()))
                        .orElse(false),
                Duration.ofSeconds(2),
                "building metadata should persist after placement"
        );
    }

    @Test
    void workshopAndBarracksAffectFutureBuildSpeedAndPromotionCost() {
        JsonMapperProvider mapperProvider = new JsonMapperProvider();
        org.tavall.control.interior.InteriorLayoutHandler layoutHandler = new org.tavall.control.interior.InteriorLayoutHandler();
        InMemoryPlayerGameStateStore gameStateStore = new InMemoryPlayerGameStateStore();
        PlayerGameStateHandler gameStateHandler = new PlayerGameStateHandler(
                gameStateStore,
                new SemanticCacheFactory(new CacheConfig("", 6379, "", false)).build("building-bonus-state"),
                new JacksonCacheCodec<>(mapperProvider.mapper(), PlayerGameState.class, "building-bonus-state"),
                mapperProvider.mapper()
        );
        PlayerSessionStore sessionStore = new PlayerSessionStore();
        StubInteriorInstanceHandler interiorInstanceHandler = new StubInteriorInstanceHandler();
        CastleBuildingHandler buildingHandler = new CastleBuildingHandler(
                sessionStore,
                gameStateHandler,
                interiorInstanceHandler,
                layoutHandler,
                mapperProvider.mapper()
        );

        UUID playerId = UUID.randomUUID();
        Instant start = Instant.parse("2026-04-15T22:15:00Z");
        PlayerGameState seededState = gameStateHandler.loadOrCreate(
                205L,
                playerId,
                new CastleLocationData("overworld", 12.0, 72.0, 12.0),
                start
        ).withResources(new ResourceInventory(200, 200, 200), start);
        seededState = gameStateHandler.setAccountLevel(seededState, 50, start);
        sessionStore.put(new PlayerSession(
                playerId,
                new PlayerProfile(205L, playerId, "BonusBot", "UTC", "hash", start, start, start),
                seededState
        ));

        String interiorWorld = interiorInstanceHandler.worldNameFor(playerId);
        Vector3d interiorOrigin = layoutHandler.originForCastle(seededState.castleLocation());
        BuildingMutationResult workshopPlacement = buildingHandler.placeBuilding(
                playerId,
                BuildingType.WORKSHOP,
                interiorWorld,
                new Vector3d(interiorOrigin.getX() + 5.0D, interiorOrigin.getY() + 1.0D, interiorOrigin.getZ()),
                start
        );
        assertTrue(workshopPlacement.changed());
        CastleBuildingData workshop = buildingHandler.listBuildings(workshopPlacement.state()).getFirst();
        PlayerGameState afterWorkshop = buildingHandler.forceComplete(playerId, workshop.buildingId(), start.plusSeconds(30)).state();

        BuildingMutationResult barracksPlacement = buildingHandler.placeBuilding(
                playerId,
                BuildingType.BARRACKS,
                interiorWorld,
                new Vector3d(interiorOrigin.getX() - 5.0D, interiorOrigin.getY() + 1.0D, interiorOrigin.getZ()),
                start.plusSeconds(31)
        );
        assertTrue(barracksPlacement.changed());
        CastleBuildingData barracks = buildingHandler.resolveBuilding(barracksPlacement.state(), BuildingType.BARRACKS.shortKey()).orElseThrow();
        PlayerGameState afterBarracks = buildingHandler.forceComplete(playerId, barracks.buildingId(), start.plusSeconds(62)).state();

        assertEquals(0.85D, buildingHandler.constructionSpeedMultiplier(afterBarracks), 0.0001D);
        PromotionCost adjustedCost = buildingHandler.adjustedPromotionCost(afterBarracks, PromotionCost.defaultCost());
        assertEquals(3, adjustedCost.foodCost());
        assertEquals(2, adjustedCost.woodCost());
        assertEquals(1, adjustedCost.ironCost());

        BuildingMutationResult farmsteadPlacement = buildingHandler.placeBuilding(
                playerId,
                BuildingType.FARMSTEAD,
                interiorWorld,
                layoutHandler.createLayoutForCastle(seededState.castleLocation()).buildingAnchor(BuildingType.FARMSTEAD),
                start.plusSeconds(63)
        );
        assertTrue(farmsteadPlacement.changed());
        CastleBuildingData farmstead = buildingHandler.resolveBuilding(farmsteadPlacement.state(), BuildingType.FARMSTEAD.shortKey()).orElseThrow();
        long buildSeconds = farmstead.constructionEndsAt().getEpochSecond() - farmstead.constructionStartedAt().getEpochSecond();
        assertEquals(21L, buildSeconds);
    }

    @Test
    void rejectsSurfacePlacementsThatBlockCastleCore() {
        JsonMapperProvider mapperProvider = new JsonMapperProvider();
        InMemoryPlayerGameStateStore gameStateStore = new InMemoryPlayerGameStateStore();
        PlayerGameStateHandler gameStateHandler = new PlayerGameStateHandler(
                gameStateStore,
                new SemanticCacheFactory(new CacheConfig("", 6379, "", false)).build("building-validate-state"),
                new JacksonCacheCodec<>(mapperProvider.mapper(), PlayerGameState.class, "building-validate-state"),
                mapperProvider.mapper()
        );
        PlayerSessionStore sessionStore = new PlayerSessionStore();
        CastleBuildingHandler buildingHandler = new CastleBuildingHandler(
                sessionStore,
                gameStateHandler,
                new StubInteriorInstanceHandler(),
                new org.tavall.control.interior.InteriorLayoutHandler(),
                mapperProvider.mapper()
        );

        UUID playerId = UUID.randomUUID();
        Instant start = Instant.parse("2026-04-15T22:25:00Z");
        PlayerGameState initialState = gameStateHandler.loadOrCreate(
                209L,
                playerId,
                new CastleLocationData("overworld", 10.0, 72.0, 10.0),
                start
        );
        initialState = gameStateHandler.setAccountLevel(initialState, 50, start);
        sessionStore.put(new PlayerSession(
                playerId,
                new PlayerProfile(209L, playerId, "ValidateBot", "UTC", "hash", start, start, start),
                initialState
        ));

        BuildingMutationResult blocked = buildingHandler.placeBuilding(
                playerId,
                BuildingType.FARMSTEAD,
                "overworld",
                new Vector3d(11.0, 73.0, 11.0),
                start
        );

        assertFalse(blocked.changed());
        assertEquals("Interior buildings must be placed inside the interior world.", blocked.message());
        assertTrue(buildingHandler.listBuildings(sessionStore.get(playerId).gameState()).isEmpty());
    }

    @Test
    void debugModeBypassesBuildingAccountLevelRestrictions() {
        JsonMapperProvider mapperProvider = new JsonMapperProvider();
        InMemoryPlayerGameStateStore gameStateStore = new InMemoryPlayerGameStateStore();
        PlayerGameStateHandler gameStateHandler = new PlayerGameStateHandler(
                gameStateStore,
                new SemanticCacheFactory(new CacheConfig("", 6379, "", false)).build("building-debug-mode"),
                new JacksonCacheCodec<>(mapperProvider.mapper(), PlayerGameState.class, "building-debug-mode"),
                mapperProvider.mapper()
        );
        PlayerSessionStore sessionStore = new PlayerSessionStore();
        StubInteriorInstanceHandler interiorInstanceHandler = new StubInteriorInstanceHandler();
        org.tavall.control.interior.InteriorLayoutHandler layoutHandler = new org.tavall.control.interior.InteriorLayoutHandler();
        CastleBuildingHandler buildingHandler = new CastleBuildingHandler(
                sessionStore,
                gameStateHandler,
                interiorInstanceHandler,
                layoutHandler,
                mapperProvider.mapper()
        );

        UUID playerId = UUID.randomUUID();
        Instant now = Instant.parse("2026-04-27T19:20:00Z");
        PlayerGameState initialState = gameStateHandler.loadOrCreate(
                610L,
                playerId,
                new CastleLocationData("overworld", 10.0, 72.0, 10.0),
                now
        ).withResources(new ResourceInventory(200, 200, 200), now);
        sessionStore.put(new PlayerSession(
                playerId,
                new PlayerProfile(610L, playerId, "DebugBuilder", "UTC", "hash", now, now, now),
                initialState
        ));

        Vector3d buildPosition = layoutHandler.createLayoutForCastle(initialState.castleLocation()).buildingAnchor(BuildingType.WORKSHOP);
        BuildingMutationResult blocked = buildingHandler.placeBuilding(
                playerId,
                BuildingType.WORKSHOP,
                interiorInstanceHandler.worldNameFor(playerId),
                buildPosition,
                now.plusSeconds(1)
        );
        assertFalse(blocked.changed());
        assertEquals("Workshop unlocks at account level 35.", blocked.message());

        PlayerGameState debugState = gameStateHandler.setDebugMode(initialState, DebugModeState.enabled(), now.plusSeconds(2));
        sessionStore.get(playerId).updateGameState(debugState);

        BuildingMutationResult allowed = buildingHandler.placeBuilding(
                playerId,
                BuildingType.WORKSHOP,
                interiorInstanceHandler.worldNameFor(playerId),
                buildPosition,
                now.plusSeconds(3)
        );

        assertTrue(allowed.changed());
        assertEquals("Workshop construction started.", allowed.message());
    }

    @Test
    void buildingMutationsPreserveInteriorInstanceIndex() {
        JsonMapperProvider mapperProvider = new JsonMapperProvider();
        InMemoryPlayerGameStateStore gameStateStore = new InMemoryPlayerGameStateStore();
        PlayerGameStateHandler gameStateHandler = new PlayerGameStateHandler(
                gameStateStore,
                new SemanticCacheFactory(new CacheConfig("", 6379, "", false)).build("building-interior-index"),
                new JacksonCacheCodec<>(mapperProvider.mapper(), PlayerGameState.class, "building-interior-index"),
                mapperProvider.mapper()
        );
        PlayerSessionStore sessionStore = new PlayerSessionStore();
        StubInteriorInstanceHandler interiorInstanceHandler = new StubInteriorInstanceHandler();
        CastleBuildingHandler buildingHandler = new CastleBuildingHandler(
                sessionStore,
                gameStateHandler,
                interiorInstanceHandler,
                new org.tavall.control.interior.InteriorLayoutHandler(),
                mapperProvider.mapper()
        );

        UUID playerId = UUID.randomUUID();
        Instant now = Instant.parse("2026-04-16T06:20:00Z");
        PlayerGameState initialState = gameStateHandler.loadOrCreate(
                511L,
                playerId,
                new CastleLocationData("overworld", 10.0, 72.0, 10.0),
                now
        ).withResources(new ResourceInventory(200, 200, 200), now);
        initialState = gameStateHandler.setAccountLevel(initialState, 50, now);
        PlayerGameState indexed = gameStateHandler.bumpInteriorInstanceIndex(initialState, now.plusSeconds(1));
        sessionStore.put(new PlayerSession(
                playerId,
                new PlayerProfile(511L, playerId, "IndexBuilder", "UTC", "hash", now, now, now),
                indexed
        ));

        assertEquals(1, gameStateHandler.interiorInstanceIndex(indexed));
        BuildingMutationResult placement = buildingHandler.placeBuilding(
                playerId,
                BuildingType.FARMSTEAD,
                interiorInstanceHandler.worldNameFor(playerId),
                new org.tavall.control.interior.InteriorLayoutHandler()
                        .createLayoutForCastle(indexed.castleLocation(), gameStateHandler.interiorInstanceIndex(indexed))
                        .buildingAnchor(BuildingType.FARMSTEAD),
                now.plusSeconds(2)
        );
        assertTrue(placement.changed());
        assertEquals(1, gameStateHandler.interiorInstanceIndex(placement.state()));
    }
}

