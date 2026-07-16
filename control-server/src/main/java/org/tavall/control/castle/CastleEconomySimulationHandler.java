package org.tavall.control.castle;
import org.tavall.control.castle.CastleEconomyPlanner;
import org.tavall.control.player.PlayerGameStateHandler;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.hypixel.hytale.server.core.HytaleServer;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.castle.ICastleBuildingHandler;
import org.tavall.control.castle.ICastleBuildingVisualHandler;
import org.tavall.control.castle.ICastleEconomySimulationHandler;
import org.tavall.control.castle.ICastleSiteVisualHandler;
import org.tavall.control.player.IPlayerGameStateHandler;
import org.tavall.control.player.IPlayerSessionStore;
import org.tavall.control.resource.IResourceNodeHandler;
import org.tavall.control.resource.IResourceNodeVisualHandler;
import org.tavall.control.api.UIData;
import org.tavall.control.domain.CastleEconomySnapshot;
import org.tavall.control.domain.CitizenMetaData;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.PopulationSummary;
import org.tavall.control.domain.ResourceInventory;
import org.tavall.control.resources.ResourceType;
import com.tjxjnoobie.api.internal.utils.concurrent.AsyncTask;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Runs the lightweight castle economy tick for active sessions.
 */
public final class CastleEconomySimulationHandler implements ICastleEconomySimulationHandler, IDependencyInjectableConcrete {
    private static final Logger LOGGER = Logger.getLogger(CastleEconomySimulationHandler.class.getName());
    public static final long TICK_INTERVAL_SECONDS = 12L;

    private final IPlayerSessionStore sessionStore;
    private final IPlayerGameStateHandler gameStateHandler;
    private final ICastleBuildingHandler buildingHandler;
    private final ICastleBuildingVisualHandler buildingVisualHandler;
    private final ICastleSiteVisualHandler castleSiteVisualHandler;
    private final CastleEconomyPlanner planner;
    private final IResourceNodeHandler resourceNodeHandler;
    private final IResourceNodeVisualHandler resourceNodeVisualHandler;
    private final UIData uiNavigator;
    private ScheduledFuture<?> tickTask;

    public CastleEconomySimulationHandler(
            IPlayerSessionStore sessionStore,
            IPlayerGameStateHandler gameStateHandler,
            ICastleBuildingHandler buildingHandler,
            ICastleBuildingVisualHandler buildingVisualHandler,
            ICastleSiteVisualHandler castleSiteVisualHandler,
            CastleEconomyPlanner planner,
            IResourceNodeHandler resourceNodeHandler,
            IResourceNodeVisualHandler resourceNodeVisualHandler,
            UIData uiNavigator
    ) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.gameStateHandler = Objects.requireNonNull(gameStateHandler, "gameStateHandler");
        this.buildingHandler = Objects.requireNonNull(buildingHandler, "buildingHandler");
        this.buildingVisualHandler = Objects.requireNonNull(buildingVisualHandler, "buildingVisualHandler");
        this.castleSiteVisualHandler = Objects.requireNonNull(castleSiteVisualHandler, "castleSiteVisualHandler");
        this.planner = Objects.requireNonNull(planner, "planner");
        this.resourceNodeHandler = Objects.requireNonNull(resourceNodeHandler, "resourceNodeHandler");
        this.resourceNodeVisualHandler = Objects.requireNonNull(resourceNodeVisualHandler, "resourceNodeVisualHandler");
        this.uiNavigator = Objects.requireNonNull(uiNavigator, "uiNavigator");
    }

    @Override
    public void start() {
        if (tickTask != null && !tickTask.isCancelled()) {
            return;
        }
        tickTask = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(
                () -> runTick(Instant.now()),
                TICK_INTERVAL_SECONDS,
                TICK_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    @Override
    public void shutdown() {
        if (tickTask != null) {
            tickTask.cancel(false);
            tickTask = null;
        }
    }

    public void runTick(Instant now) {
        for (PlayerSession session : sessionStore.snapshot()) {
            applyTick(session, now);
        }
    }

    private void applyTick(PlayerSession session, Instant now) {
        PlayerGameState state = session.gameState();
        CastleEconomySnapshot snapshot = planner.snapshot(state);
        CitizenMetaData citizenMetaData = state.populationSummary().citizenMetaData().withJobCounts(snapshot.jobCounts());
        PopulationSummary updatedSummary = new PopulationSummary(
                state.populationSummary().citizenCount(),
                state.populationSummary().troopCount(),
                citizenMetaData,
                state.populationSummary().troopMetaData(),
                state.populationSummary().agingState().tick(now)
        );
        ResourceInventory updatedResources = state.resources()
                .withFood(state.resources().food() + snapshot.gainFor(ResourceType.FOOD))
                .withWood(state.resources().wood() + snapshot.gainFor(ResourceType.WOOD))
                .withIron(state.resources().iron() + snapshot.gainFor(ResourceType.IRON));
        PlayerGameState updatedState = state.withPopulation(updatedSummary, now).withResources(updatedResources, now);
        updatedState = resourceNodeHandler.applyTick(updatedState, snapshot, now);
        updatedState = buildingHandler.applyTick(session.playerId(), updatedState, now);
        session.updateGameState(updatedState);
        gameStateHandler.cacheState(session.playerId(), updatedState);
        castleSiteVisualHandler.refreshSite(session.playerId(), updatedState);
        buildingVisualHandler.refreshBuildings(session.playerId(), updatedState);
        resourceNodeVisualHandler.refreshNodes(session.playerId(), updatedState);
        uiNavigator.refreshTrackedPage(session.playerId(), updatedState);
        PlayerGameState persistedState = updatedState;
        AsyncTask.runAsync(() -> gameStateHandler.persistState(persistedState, now));
        LOGGER.fine(() -> "Economy tick applied for " + session.playerId());
    }
}
