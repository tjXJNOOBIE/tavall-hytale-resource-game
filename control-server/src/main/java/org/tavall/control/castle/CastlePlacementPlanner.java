package org.tavall.control.castle;
import org.tavall.control.player.PlayerGameStateHandler;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.castle.ICastleBuildingVisualHandler;
import org.tavall.control.castle.ICastlePlacementHandler;
import org.tavall.control.castle.ICastleSiteVisualHandler;
import org.tavall.control.castle.ICastleSpawnHandler;
import org.tavall.control.player.IPlayerGameStateHandler;
import org.tavall.control.player.IPlayerSessionStore;
import org.tavall.control.resource.IResourceNodeVisualHandler;
import org.tavall.control.domain.CastleLocationData;
import org.tavall.control.domain.PlayerGameState;
import com.tjxjnoobie.api.internal.utils.concurrent.AsyncTask;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Relocates a player's castle and refreshes all dependent world visuals.
 */
public final class CastlePlacementPlanner implements ICastlePlacementHandler, IDependencyInjectableConcrete {
    private final IPlayerSessionStore sessionStore;
    private final IPlayerGameStateHandler gameStateHandler;
    private final ICastleSpawnHandler castleSpawnHandler;
    private final ICastleSiteVisualHandler castleSiteVisualHandler;
    private final ICastleBuildingVisualHandler buildingVisualHandler;
    private final IResourceNodeVisualHandler resourceNodeVisualHandler;

    public CastlePlacementPlanner(
            IPlayerSessionStore sessionStore,
            IPlayerGameStateHandler gameStateHandler,
            ICastleSpawnHandler castleSpawnHandler,
            ICastleSiteVisualHandler castleSiteVisualHandler,
            ICastleBuildingVisualHandler buildingVisualHandler,
            IResourceNodeVisualHandler resourceNodeVisualHandler
    ) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.gameStateHandler = Objects.requireNonNull(gameStateHandler, "gameStateHandler");
        this.castleSpawnHandler = Objects.requireNonNull(castleSpawnHandler, "castleSpawnHandler");
        this.castleSiteVisualHandler = Objects.requireNonNull(castleSiteVisualHandler, "castleSiteVisualHandler");
        this.buildingVisualHandler = Objects.requireNonNull(buildingVisualHandler, "buildingVisualHandler");
        this.resourceNodeVisualHandler = Objects.requireNonNull(resourceNodeVisualHandler, "resourceNodeVisualHandler");
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
        gameStateHandler.cacheState(playerId, updatedState);
        castleSpawnHandler.replaceCastle(playerId, castleLocation);
        castleSiteVisualHandler.refreshSite(playerId, updatedState);
        buildingVisualHandler.refreshBuildings(playerId, updatedState);
        resourceNodeVisualHandler.refreshNodes(playerId, updatedState);
        AsyncTask.runAsync(() -> gameStateHandler.persistState(updatedState, now));
        return updatedState;
    }
}

