package org.tavall.control.runtime;
import org.tavall.control.player.PlayerGameStateService;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.dependency.interfaces.ICastleSiteVisualService;
import org.tavall.control.dependency.interfaces.IPlayerGameStateService;
import org.tavall.control.dependency.interfaces.IPlayerSessionStore;
import org.tavall.control.dependency.interfaces.IResourceService;
import org.tavall.control.dependency.interfaces.IUiNavigator;
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
public final class ResourceService implements IResourceService, IDependencyInjectableConcrete {
    private final IPlayerSessionStore sessionStore;
    private final IPlayerGameStateService gameStateService;
    private final ICastleSiteVisualService castleSiteVisualService;
    private final IUiNavigator uiNavigator;

    public ResourceService(
            IPlayerSessionStore sessionStore,
            IPlayerGameStateService gameStateService,
            ICastleSiteVisualService castleSiteVisualService,
            IUiNavigator uiNavigator
    ) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.gameStateService = Objects.requireNonNull(gameStateService, "gameStateService");
        this.castleSiteVisualService = Objects.requireNonNull(castleSiteVisualService, "castleSiteVisualService");
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
        castleSiteVisualService.refreshSite(playerId, updatedState);
        uiNavigator.refreshTrackedPage(playerId, updatedState);
        gameStateService.cacheState(playerId, updatedState);
        AsyncTask.runAsync(() -> gameStateService.persistState(updatedState, Instant.now()));
        return updatedState;
    }
}
