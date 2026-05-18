package org.tavall.control.population;
import org.tavall.control.population.PopulationDisplayGateway;
import org.tavall.control.player.PlayerGameStateHandler;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.castle.ICastleBuildingHandler;
import org.tavall.control.castle.ICastleSiteVisualHandler;
import org.tavall.control.player.IPlayerGameStateHandler;
import org.tavall.control.player.IPlayerSessionStore;
import org.tavall.control.population.IPopulationHandler;
import org.tavall.control.resource.IResourceHandler;
import org.tavall.control.resource.IResourceNodeHandler;
import org.tavall.control.resource.IResourceNodeVisualHandler;
import org.tavall.control.ui.IUiNavigator;
import org.tavall.control.domain.AgingState;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.PopulationSummary;
import org.tavall.control.domain.ResourceInventory;
import org.tavall.control.population.PromotionCost;
import org.tavall.control.ui.UpgradeActionState;

import com.tjxjnoobie.api.internal.utils.concurrent.AsyncTask;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Manages citizen and troop counts as a single continuum.
 */
public final class PopulationHandler implements IPopulationHandler, IDependencyInjectableConcrete {
    private final IPlayerSessionStore sessionStore;
    private final IPlayerGameStateHandler gameStateHandler;
    private final IResourceHandler resourceHandler;
    private final ICastleSiteVisualHandler castleSiteVisualHandler;
    private final PopulationDisplayGateway displayHandler;
    private final PromotionCost promotionCost;
    private final ICastleBuildingHandler buildingHandler;
    private final IResourceNodeHandler resourceNodeHandler;
    private final IResourceNodeVisualHandler resourceNodeVisualHandler;
    private final IUiNavigator uiNavigator;

    public PopulationHandler(
            IPlayerSessionStore sessionStore,
            IPlayerGameStateHandler gameStateHandler,
            IResourceHandler resourceHandler,
            ICastleSiteVisualHandler castleSiteVisualHandler,
            PopulationDisplayGateway displayHandler,
            PromotionCost promotionCost,
            ICastleBuildingHandler buildingHandler,
            IResourceNodeHandler resourceNodeHandler,
            IResourceNodeVisualHandler resourceNodeVisualHandler,
            IUiNavigator uiNavigator
    ) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.gameStateHandler = Objects.requireNonNull(gameStateHandler, "gameStateHandler");
        this.resourceHandler = Objects.requireNonNull(resourceHandler, "resourceHandler");
        this.castleSiteVisualHandler = Objects.requireNonNull(castleSiteVisualHandler, "castleSiteVisualHandler");
        this.displayHandler = Objects.requireNonNull(displayHandler, "displayHandler");
        this.promotionCost = Objects.requireNonNull(promotionCost, "promotionCost");
        this.buildingHandler = Objects.requireNonNull(buildingHandler, "buildingHandler");
        this.resourceNodeHandler = Objects.requireNonNull(resourceNodeHandler, "resourceNodeHandler");
        this.resourceNodeVisualHandler = Objects.requireNonNull(resourceNodeVisualHandler, "resourceNodeVisualHandler");
        this.uiNavigator = Objects.requireNonNull(uiNavigator, "uiNavigator");
    }

    public PlayerGameState addCitizens(UUID playerId, int amount) {
        return updatePopulation(playerId, amount, 0);
    }

    public PlayerGameState setCitizens(UUID playerId, int count) {
        PlayerSession session = sessionStore.get(playerId);
        if (session == null) {
            return null;
        }
        int delta = count - session.gameState().populationSummary().citizenCount();
        return updatePopulation(playerId, delta, 0);
    }

    public PlayerGameState addTroops(UUID playerId, int amount) {
        return updatePopulation(playerId, 0, amount);
    }

    public PlayerGameState setTroops(UUID playerId, int count) {
        PlayerSession session = sessionStore.get(playerId);
        if (session == null) {
            return null;
        }
        int delta = count - session.gameState().populationSummary().troopCount();
        return updatePopulation(playerId, 0, delta);
    }

    public boolean promoteCitizen(UUID playerId) {
        PlayerSession session = sessionStore.get(playerId);
        if (session == null) {
            return false;
        }
        PopulationSummary summary = session.gameState().populationSummary();
        if (summary.citizenCount() <= 0) {
            return false;
        }
        ResourceInventory resources = session.gameState().resources();
        PromotionCost adjustedCost = buildingHandler.adjustedPromotionCost(session.gameState(), promotionCost);
        if (resources.food() < adjustedCost.foodCost()
                || resources.wood() < adjustedCost.woodCost()
                || resources.iron() < adjustedCost.ironCost()) {
            return false;
        }
        ResourceInventory updatedResources = resources
                .withFood(resources.food() - adjustedCost.foodCost())
                .withWood(resources.wood() - adjustedCost.woodCost())
                .withIron(resources.iron() - adjustedCost.ironCost());
        return updatePopulation(playerId, -1, 1, updatedResources) != null;
    }

    public boolean demoteTroop(UUID playerId) {
        PlayerSession session = sessionStore.get(playerId);
        if (session == null) {
            return false;
        }
        PopulationSummary summary = session.gameState().populationSummary();
        if (summary.troopCount() <= 0) {
            return false;
        }
        updatePopulation(playerId, 1, -1);
        return true;
    }

    public UpgradeActionState promoteActionState(PlayerGameState state) {
        PopulationSummary summary = state.populationSummary();
        PromotionCost adjustedCost = buildingHandler.adjustedPromotionCost(state, promotionCost);
        if (summary.citizenCount() <= 0) {
            return new UpgradeActionState(false, "Blocked: need at least 1 citizen.");
        }
        ResourceInventory resources = state.resources();
        if (resources.food() < adjustedCost.foodCost()) {
            return new UpgradeActionState(false, "Blocked: need " + adjustedCost.foodCost() + " Food.");
        }
        if (resources.wood() < adjustedCost.woodCost()) {
            return new UpgradeActionState(false, "Blocked: need " + adjustedCost.woodCost() + " Wood.");
        }
        if (resources.iron() < adjustedCost.ironCost()) {
            return new UpgradeActionState(false, "Blocked: need " + adjustedCost.ironCost() + " Iron.");
        }
        return new UpgradeActionState(true, "Ready: promote 1 citizen into 1 troop.");
    }

    public UpgradeActionState demoteActionState(PlayerGameState state) {
        if (state.populationSummary().troopCount() <= 0) {
            return new UpgradeActionState(false, "Blocked: need at least 1 troop.");
        }
        return new UpgradeActionState(true, "Ready: return 1 troop to the citizen pool.");
    }

    public String promotionCostSummary(PlayerGameState state) {
        PromotionCost adjustedCost = buildingHandler.adjustedPromotionCost(state, promotionCost);
        // UI pages call this with live state-specific values elsewhere when they need exact discounts.
        return "Cost per promotion: "
                + adjustedCost.foodCost() + " Food, "
                + adjustedCost.woodCost() + " Wood, "
                + adjustedCost.ironCost() + " Iron.";
    }

    public PlayerGameState updateAging(UUID playerId, Instant now) {
        PlayerSession session = sessionStore.get(playerId);
        if (session == null) {
            return null;
        }
        PopulationSummary summary = session.gameState().populationSummary();
        AgingState aged = summary.agingState().tick(now);
        PopulationSummary updatedSummary = summary.withAgingState(aged);
        PlayerGameState updated = session.gameState().withPopulation(updatedSummary, now);
        session.updateGameState(updated);
        gameStateHandler.cacheState(playerId, updated);
        AsyncTask.runAsync(() -> gameStateHandler.persistState(updated, now));
        return updated;
    }

    private PlayerGameState updatePopulation(UUID playerId, int citizenDelta, int troopDelta) {
        return updatePopulation(playerId, citizenDelta, troopDelta, null);
    }

    private PlayerGameState updatePopulation(UUID playerId, int citizenDelta, int troopDelta, ResourceInventory resourceOverride) {
        PlayerSession session = sessionStore.get(playerId);
        if (session == null) {
            return null;
        }
        PopulationSummary summary = session.gameState().populationSummary();
        int citizens = Math.max(0, summary.citizenCount() + citizenDelta);
        int troops = Math.max(0, summary.troopCount() + troopDelta);
        PopulationSummary updatedSummary = new PopulationSummary(
                citizens,
                troops,
                summary.citizenMetaData(),
                summary.troopMetaData(),
                summary.agingState()
        );
        Instant now = Instant.now();
        PlayerGameState updated = session.gameState().withPopulation(updatedSummary, now);
        if (resourceOverride != null) {
            updated = updated.withResources(resourceOverride, now);
        }
        updated = resourceNodeHandler.normalizeAssignments(updated, now);
        session.updateGameState(updated);
        displayHandler.updateDisplays(playerId, updatedSummary);
        castleSiteVisualHandler.refreshSite(playerId, updated);
        resourceNodeVisualHandler.refreshNodes(playerId, updated);
        uiNavigator.refreshTrackedPage(playerId, updated);
        gameStateHandler.cacheState(playerId, updated);
        PlayerGameState persistedState = updated;
        AsyncTask.runAsync(() -> gameStateHandler.persistState(persistedState, now));
        return updated;
    }
}

