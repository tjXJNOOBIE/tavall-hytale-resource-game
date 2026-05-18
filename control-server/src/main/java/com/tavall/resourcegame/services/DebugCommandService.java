package org.tavall.control.runtime;
import org.tavall.control.player.PlayerGameStateService;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.dependency.interfaces.ICastlePromptLaneService;
import org.tavall.control.dependency.interfaces.ICastleBuildingService;
import org.tavall.control.dependency.interfaces.ICastleBuildingVisualService;
import org.tavall.control.dependency.interfaces.ICastleSiteVisualService;
import org.tavall.control.dependency.interfaces.ICastleSpawnService;
import org.tavall.control.dependency.interfaces.ICastleEconomySimulationService;
import org.tavall.control.dependency.interfaces.IDebugCommandService;
import org.tavall.control.dependency.interfaces.IFocusedWorldOverrideService;
import org.tavall.control.dependency.interfaces.IFrontendCommandVerificationService;
import org.tavall.control.dependency.interfaces.IInfrastructureHealthService;
import org.tavall.control.dependency.interfaces.IInteriorWorldService;
import org.tavall.control.dependency.interfaces.IPlacementModeService;
import org.tavall.control.dependency.interfaces.IPlayerDataService;
import org.tavall.control.dependency.interfaces.IPlayerGameStateService;
import org.tavall.control.dependency.interfaces.IPlayerSessionStore;
import org.tavall.control.dependency.interfaces.IPlayerTeleportService;
import org.tavall.control.dependency.interfaces.IPopulationService;
import org.tavall.control.dependency.interfaces.IResourceNodeService;
import org.tavall.control.dependency.interfaces.IResourceNodeVisualService;
import org.tavall.control.dependency.interfaces.IResourceService;
import org.tavall.control.dependency.interfaces.IUiNavigator;
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
public final class DebugCommandService implements IDebugCommandService, IDependencyInjectableConcrete {
    private final IPlayerSessionStore sessionStore;
    private final IUiNavigator uiNavigator;
    private final IPopulationService populationService;
    private final IResourceService resourceService;
    private final IInteriorWorldService interiorWorldService;
    private final ICastleSpawnService castleSpawnService;
    private final ICastlePromptLaneService castlePromptLaneService;
    private final IFocusedWorldOverrideService focusedWorldOverrideService;
    private final IPlayerDataService playerDataService;
    private final IPlayerGameStateService gameStateService;
    private final IInfrastructureHealthService infrastructureHealthService;
    private final ICastleBuildingService buildingService;
    private final ICastleBuildingVisualService buildingVisualService;
    private final IResourceNodeService resourceNodeService;
    private final IResourceNodeVisualService resourceNodeVisualService;
    private final ICastleSiteVisualService castleSiteVisualService;
    private final ICastleEconomySimulationService castleEconomySimulationService;
    private final IPlayerTeleportService playerTeleportService;
    private final IPlacementModeService placementModeService;
    private final KingdomBuildingCommandSupport buildingCommandSupport;
    private final KingdomNodeCommandSupport nodeCommandSupport;
    private final KingdomPlacementCommandSupport placementCommandSupport;
    private final KingdomInteractionCommandSupport interactionCommandSupport;
    private final KingdomHologramCommandSupport hologramCommandSupport;
    private final KingdomEntityCommandSupport entityCommandSupport;
    private final IFrontendCommandVerificationService frontendCommandVerificationService;

    public DebugCommandService(
            IPlayerSessionStore sessionStore,
            IUiNavigator uiNavigator,
            IPopulationService populationService,
            IResourceService resourceService,
            IInteriorWorldService interiorWorldService,
            ICastleSpawnService castleSpawnService,
            ICastlePromptLaneService castlePromptLaneService,
            IFocusedWorldOverrideService focusedWorldOverrideService,
            IPlayerDataService playerDataService,
            IPlayerGameStateService gameStateService,
            IInfrastructureHealthService infrastructureHealthService,
            ICastleBuildingService buildingService,
            ICastleBuildingVisualService buildingVisualService,
            IResourceNodeService resourceNodeService,
            IResourceNodeVisualService resourceNodeVisualService,
            ICastleSiteVisualService castleSiteVisualService,
            ICastleEconomySimulationService castleEconomySimulationService,
            IPlayerTeleportService playerTeleportService,
            IPlacementModeService placementModeService,
            KingdomBuildingCommandSupport buildingCommandSupport,
            KingdomNodeCommandSupport nodeCommandSupport,
            KingdomPlacementCommandSupport placementCommandSupport,
            KingdomInteractionCommandSupport interactionCommandSupport,
            KingdomHologramCommandSupport hologramCommandSupport,
            KingdomEntityCommandSupport entityCommandSupport,
            IFrontendCommandVerificationService frontendCommandVerificationService
    ) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.uiNavigator = Objects.requireNonNull(uiNavigator, "uiNavigator");
        this.populationService = Objects.requireNonNull(populationService, "populationService");
        this.resourceService = Objects.requireNonNull(resourceService, "resourceService");
        this.interiorWorldService = Objects.requireNonNull(interiorWorldService, "interiorWorldService");
        this.castleSpawnService = Objects.requireNonNull(castleSpawnService, "castleSpawnService");
        this.castlePromptLaneService = Objects.requireNonNull(castlePromptLaneService, "castlePromptLaneService");
        this.focusedWorldOverrideService = Objects.requireNonNull(focusedWorldOverrideService, "focusedWorldOverrideService");
        this.playerDataService = Objects.requireNonNull(playerDataService, "playerDataService");
        this.gameStateService = Objects.requireNonNull(gameStateService, "gameStateService");
        this.infrastructureHealthService = Objects.requireNonNull(infrastructureHealthService, "infrastructureHealthService");
        this.buildingService = Objects.requireNonNull(buildingService, "buildingService");
        this.buildingVisualService = Objects.requireNonNull(buildingVisualService, "buildingVisualService");
        this.resourceNodeService = Objects.requireNonNull(resourceNodeService, "resourceNodeService");
        this.resourceNodeVisualService = Objects.requireNonNull(resourceNodeVisualService, "resourceNodeVisualService");
        this.castleSiteVisualService = Objects.requireNonNull(castleSiteVisualService, "castleSiteVisualService");
        this.castleEconomySimulationService = Objects.requireNonNull(castleEconomySimulationService, "castleEconomySimulationService");
        this.playerTeleportService = Objects.requireNonNull(playerTeleportService, "playerTeleportService");
        this.placementModeService = Objects.requireNonNull(placementModeService, "placementModeService");
        this.buildingCommandSupport = Objects.requireNonNull(buildingCommandSupport, "buildingCommandSupport");
        this.nodeCommandSupport = Objects.requireNonNull(nodeCommandSupport, "nodeCommandSupport");
        this.placementCommandSupport = Objects.requireNonNull(placementCommandSupport, "placementCommandSupport");
        this.interactionCommandSupport = Objects.requireNonNull(interactionCommandSupport, "interactionCommandSupport");
        this.hologramCommandSupport = Objects.requireNonNull(hologramCommandSupport, "hologramCommandSupport");
        this.entityCommandSupport = Objects.requireNonNull(entityCommandSupport, "entityCommandSupport");
        this.frontendCommandVerificationService = Objects.requireNonNull(frontendCommandVerificationService, "frontendCommandVerificationService");
    }

    public List<AbstractAsyncCommand> commands() {
        AbstractAsyncCommand kingdom = new KingdomCommand("kingdom");
        return List.of(kingdom);
 }
}
