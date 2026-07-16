package org.tavall.control.runtime;
import org.tavall.control.player.PlayerGameStateHandler;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.castle.ICastlePromptLaneHandler;
import org.tavall.control.castle.ICastleBuildingHandler;
import org.tavall.control.castle.ICastleBuildingVisualHandler;
import org.tavall.control.castle.ICastleSiteVisualHandler;
import org.tavall.control.castle.ICastleSpawnHandler;
import org.tavall.control.castle.ICastleEconomySimulationHandler;
import org.tavall.control.runtime.IDebugCommandHandler;
import org.tavall.control.world.IFocusedWorldOverrideHandler;
import org.tavall.control.runtime.IFrontendCommandVerificationHandler;
import org.tavall.control.runtime.IInfrastructureHealthHandler;
import org.tavall.control.interior.IInteriorWorldHandler;
import org.tavall.control.building.IPlacementModeHandler;
import org.tavall.control.player.IPlayerDataHandler;
import org.tavall.control.player.IPlayerGameStateHandler;
import org.tavall.control.player.IPlayerSessionStore;
import org.tavall.control.player.IPlayerTeleportHandler;
import org.tavall.control.population.IPopulationHandler;
import org.tavall.control.resource.IResourceNodeHandler;
import org.tavall.control.resource.IResourceNodeVisualHandler;
import org.tavall.control.resource.IResourceHandler;
import org.tavall.control.api.UIData;
import org.tavall.control.commands.KingdomInteractionCommandSupport;
import org.tavall.control.commands.KingdomBuildingCommandSupport;
import org.tavall.control.commands.KingdomCommand;
import org.tavall.control.commands.KingdomEntityCommandSupport;
import org.tavall.control.commands.KingdomHologramCommandSupport;
import org.tavall.control.commands.KingdomNodeCommandSupport;
import org.tavall.control.commands.KingdomPlacementCommandSupport;
import java.util.List;
import java.util.Objects;

/**
 * Builds debug command instances.
 */
public final class DebugCommandHandler implements IDebugCommandHandler, IDependencyInjectableConcrete {
    private final IPlayerSessionStore sessionStore;
    private final UIData uiNavigator;
    private final IPopulationHandler populationHandler;
    private final IResourceHandler resourceHandler;
    private final IInteriorWorldHandler interiorWorldHandler;
    private final ICastleSpawnHandler castleSpawnHandler;
    private final ICastlePromptLaneHandler castlePromptLaneHandler;
    private final IFocusedWorldOverrideHandler focusedWorldOverrideHandler;
    private final IPlayerDataHandler playerDataHandler;
    private final IPlayerGameStateHandler gameStateHandler;
    private final IInfrastructureHealthHandler infrastructureHealthHandler;
    private final ICastleBuildingHandler buildingHandler;
    private final ICastleBuildingVisualHandler buildingVisualHandler;
    private final IResourceNodeHandler resourceNodeHandler;
    private final IResourceNodeVisualHandler resourceNodeVisualHandler;
    private final ICastleSiteVisualHandler castleSiteVisualHandler;
    private final ICastleEconomySimulationHandler castleEconomySimulationHandler;
    private final IPlayerTeleportHandler playerTeleportHandler;
    private final IPlacementModeHandler placementModeHandler;
    private final KingdomBuildingCommandSupport buildingCommandSupport;
    private final KingdomNodeCommandSupport nodeCommandSupport;
    private final KingdomPlacementCommandSupport placementCommandSupport;
    private final KingdomInteractionCommandSupport interactionCommandSupport;
    private final KingdomHologramCommandSupport hologramCommandSupport;
    private final KingdomEntityCommandSupport entityCommandSupport;
    private final IFrontendCommandVerificationHandler frontendCommandVerificationHandler;

    public DebugCommandHandler(
            IPlayerSessionStore sessionStore,
            UIData uiNavigator,
            IPopulationHandler populationHandler,
            IResourceHandler resourceHandler,
            IInteriorWorldHandler interiorWorldHandler,
            ICastleSpawnHandler castleSpawnHandler,
            ICastlePromptLaneHandler castlePromptLaneHandler,
            IFocusedWorldOverrideHandler focusedWorldOverrideHandler,
            IPlayerDataHandler playerDataHandler,
            IPlayerGameStateHandler gameStateHandler,
            IInfrastructureHealthHandler infrastructureHealthHandler,
            ICastleBuildingHandler buildingHandler,
            ICastleBuildingVisualHandler buildingVisualHandler,
            IResourceNodeHandler resourceNodeHandler,
            IResourceNodeVisualHandler resourceNodeVisualHandler,
            ICastleSiteVisualHandler castleSiteVisualHandler,
            ICastleEconomySimulationHandler castleEconomySimulationHandler,
            IPlayerTeleportHandler playerTeleportHandler,
            IPlacementModeHandler placementModeHandler,
            KingdomBuildingCommandSupport buildingCommandSupport,
            KingdomNodeCommandSupport nodeCommandSupport,
            KingdomPlacementCommandSupport placementCommandSupport,
            KingdomInteractionCommandSupport interactionCommandSupport,
            KingdomHologramCommandSupport hologramCommandSupport,
            KingdomEntityCommandSupport entityCommandSupport,
            IFrontendCommandVerificationHandler frontendCommandVerificationHandler
    ) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.uiNavigator = Objects.requireNonNull(uiNavigator, "uiNavigator");
        this.populationHandler = Objects.requireNonNull(populationHandler, "populationHandler");
        this.resourceHandler = Objects.requireNonNull(resourceHandler, "resourceHandler");
        this.interiorWorldHandler = Objects.requireNonNull(interiorWorldHandler, "interiorWorldHandler");
        this.castleSpawnHandler = Objects.requireNonNull(castleSpawnHandler, "castleSpawnHandler");
        this.castlePromptLaneHandler = Objects.requireNonNull(castlePromptLaneHandler, "castlePromptLaneHandler");
        this.focusedWorldOverrideHandler = Objects.requireNonNull(focusedWorldOverrideHandler, "focusedWorldOverrideHandler");
        this.playerDataHandler = Objects.requireNonNull(playerDataHandler, "playerDataHandler");
        this.gameStateHandler = Objects.requireNonNull(gameStateHandler, "gameStateHandler");
        this.infrastructureHealthHandler = Objects.requireNonNull(infrastructureHealthHandler, "infrastructureHealthHandler");
        this.buildingHandler = Objects.requireNonNull(buildingHandler, "buildingHandler");
        this.buildingVisualHandler = Objects.requireNonNull(buildingVisualHandler, "buildingVisualHandler");
        this.resourceNodeHandler = Objects.requireNonNull(resourceNodeHandler, "resourceNodeHandler");
        this.resourceNodeVisualHandler = Objects.requireNonNull(resourceNodeVisualHandler, "resourceNodeVisualHandler");
        this.castleSiteVisualHandler = Objects.requireNonNull(castleSiteVisualHandler, "castleSiteVisualHandler");
        this.castleEconomySimulationHandler = Objects.requireNonNull(castleEconomySimulationHandler, "castleEconomySimulationHandler");
        this.playerTeleportHandler = Objects.requireNonNull(playerTeleportHandler, "playerTeleportHandler");
        this.placementModeHandler = Objects.requireNonNull(placementModeHandler, "placementModeHandler");
        this.buildingCommandSupport = Objects.requireNonNull(buildingCommandSupport, "buildingCommandSupport");
        this.nodeCommandSupport = Objects.requireNonNull(nodeCommandSupport, "nodeCommandSupport");
        this.placementCommandSupport = Objects.requireNonNull(placementCommandSupport, "placementCommandSupport");
        this.interactionCommandSupport = Objects.requireNonNull(interactionCommandSupport, "interactionCommandSupport");
        this.hologramCommandSupport = Objects.requireNonNull(hologramCommandSupport, "hologramCommandSupport");
        this.entityCommandSupport = Objects.requireNonNull(entityCommandSupport, "entityCommandSupport");
        this.frontendCommandVerificationHandler = Objects.requireNonNull(frontendCommandVerificationHandler, "frontendCommandVerificationHandler");
    }

    public List<AbstractAsyncCommand> commands() {
        AbstractAsyncCommand kingdom = new KingdomCommand("kingdom");
        return List.of(kingdom);
 }
}
