package com.tavall.resourcegame.services;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.dependency.interfaces.ICastleBuildingVisualService;
import com.tavall.resourcegame.dependency.interfaces.ICastlePlacementService;
import com.tavall.resourcegame.dependency.interfaces.ICastleSiteVisualService;
import com.tavall.resourcegame.dependency.interfaces.ICastleSpawnService;
import com.tavall.resourcegame.dependency.interfaces.IPlayerGameStateService;
import com.tavall.resourcegame.dependency.interfaces.IPlayerSessionStore;
import com.tavall.resourcegame.dependency.interfaces.IResourceNodeVisualService;
import com.tavall.resourcegame.domain.CastleLocationData;
import com.tavall.resourcegame.domain.PlayerGameState;
import com.tjxjnoobie.api.internal.utils.concurrent.AsyncTask;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Relocates a player's castle and refreshes all dependent world visuals.
 */
public final class CastlePlacementService implements ICastlePlacementService, IDependencyInjectableConcrete {
    private final IPlayerSessionStore sessionStore;
    private final IPlayerGameStateService gameStateService;
    private final ICastleSpawnService castleSpawnService;
    private final ICastleSiteVisualService castleSiteVisualService;
    private final ICastleBuildingVisualService buildingVisualService;
    private final IResourceNodeVisualService resourceNodeVisualService;

    public CastlePlacementService(
            IPlayerSessionStore sessionStore,
            IPlayerGameStateService gameStateService,
            ICastleSpawnService castleSpawnService,
            ICastleSiteVisualService castleSiteVisualService,
            ICastleBuildingVisualService buildingVisualService,
            IResourceNodeVisualService resourceNodeVisualService
    ) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.gameStateService = Objects.requireNonNull(gameStateService, "gameStateService");
        this.castleSpawnService = Objects.requireNonNull(castleSpawnService, "castleSpawnService");
        this.castleSiteVisualService = Objects.requireNonNull(castleSiteVisualService, "castleSiteVisualService");
        this.buildingVisualService = Objects.requireNonNull(buildingVisualService, "buildingVisualService");
        this.resourceNodeVisualService = Objects.requireNonNull(resourceNodeVisualService, "resourceNodeVisualService");
    }

    @Override
    public PlayerGameState placeCastle(UUID playerId, CastleLocationData castleLocation, Instant now) {
        PlayerSession session = sessionStore.get(playerId);
        if (session == null || castleLocation == null) {
            return null;
        }
        PlayerGameState updatedState = session.gameState().withCastleLocation(
                castleLocation,
                session.gameState().castleId() == null ? UUID.randomUUID() : session.gameState().castleId(),
                session.gameState().castleAssetType(),
                now
        );
        session.updateGameState(updatedState);
        gameStateService.cacheState(playerId, updatedState);
        castleSpawnService.replaceCastle(playerId, castleLocation);
        castleSiteVisualService.refreshSite(playerId, updatedState);
        buildingVisualService.refreshBuildings(playerId, updatedState);
        resourceNodeVisualService.refreshNodes(playerId, updatedState);
        AsyncTask.runAsync(() -> gameStateService.persistState(updatedState, now));
        return updatedState;
    }
}
