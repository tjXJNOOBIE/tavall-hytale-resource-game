package org.tavall.control.resource;
import org.tavall.control.player.PlayerGameStateHandler;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.castle.ICastleSiteVisualHandler;
import org.tavall.control.player.IPlayerGameStateHandler;
import org.tavall.control.player.IPlayerSessionStore;
import org.tavall.control.resource.IResourceHandler;
import org.tavall.control.api.UIData;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.ResourceInventory;
import org.tavall.control.resources.ResourceType;

import com.tjxjnoobie.api.internal.utils.concurrent.AsyncTask;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Mutates resources for a player session.
 */
public final class ResourceHandler implements IResourceHandler, IDependencyInjectableConcrete {
    private final IPlayerSessionStore sessionStore;
    private final IPlayerGameStateHandler gameStateHandler;
    private final ICastleSiteVisualHandler castleSiteVisualHandler;
    private final UIData uiNavigator;

    public ResourceHandler(
            IPlayerSessionStore sessionStore,
            IPlayerGameStateHandler gameStateHandler,
            ICastleSiteVisualHandler castleSiteVisualHandler,
            UIData uiNavigator
    ) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.gameStateHandler = Objects.requireNonNull(gameStateHandler, "gameStateHandler");
        this.castleSiteVisualHandler = Objects.requireNonNull(castleSiteVisualHandler, "castleSiteVisualHandler");
        this.uiNavigator = Objects.requireNonNull(uiNavigator, "uiNavigator");
    }

    public PlayerGameState addResource(UUID playerId, ResourceType type, int amount) {
        return updateResource(playerId, type, currentValue(playerId, type) + amount);
    }

    public PlayerGameState setResource(UUID playerId, ResourceType type, int amount) {
        return updateResource(playerId, type, amount);
    }

    private int currentValue(UUID playerId, ResourceType type) {
        PlayerSession session = sessionStore.get(playerId);
        if (session == null) {
            return 0;
        }
        ResourceInventory resources = session.gameState().resources();
        return switch (type) {
            case FOOD -> resources.food();
            case WOOD -> resources.wood();
            case IRON -> resources.iron();
        };
    }

    private PlayerGameState updateResource(UUID playerId, ResourceType type, int amount) {
        PlayerSession session = sessionStore.get(playerId);
        if (session == null) {
            return null;
        }
        ResourceInventory resources = session.gameState().resources();
        ResourceInventory updated = switch (type) {
            case FOOD -> resources.withFood(amount);
            case WOOD -> resources.withWood(amount);
            case IRON -> resources.withIron(amount);
        };
        PlayerGameState updatedState = session.gameState().withResources(updated, Instant.now());
        session.updateGameState(updatedState);
        castleSiteVisualHandler.refreshSite(playerId, updatedState);
        uiNavigator.refreshTrackedPage(playerId, updatedState);
        gameStateHandler.cacheState(playerId, updatedState);
        AsyncTask.runAsync(() -> gameStateHandler.persistState(updatedState, Instant.now()));
        return updatedState;
    }
}
