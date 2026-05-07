package com.tavall.hytale.resourcegame.ui;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.tavall.hytale.resourcegame.dependency.IDependencyInjectableConcrete;
import com.tavall.hytale.resourcegame.dependency.interfaces.ICastleBuildingService;
import com.tavall.hytale.resourcegame.dependency.interfaces.ICastleBuildingVisualService;
import com.tavall.hytale.resourcegame.dependency.interfaces.ICastleSiteVisualService;
import com.tavall.hytale.resourcegame.dependency.interfaces.ICastleSpawnService;
import com.tavall.hytale.resourcegame.dependency.interfaces.ICustomEntitySpawnService;
import com.tavall.hytale.resourcegame.dependency.interfaces.IFocusedWorldInteractionService;
import com.tavall.hytale.resourcegame.dependency.interfaces.IInteriorWorldService;
import com.tavall.hytale.resourcegame.dependency.interfaces.IPlacementModeService;
import com.tavall.hytale.resourcegame.dependency.interfaces.IPlayerGameStateService;
import com.tavall.hytale.resourcegame.dependency.interfaces.IPlayerSessionStore;
import com.tavall.hytale.resourcegame.dependency.interfaces.IPopulationService;
import com.tavall.hytale.resourcegame.dependency.interfaces.IResourceNodeService;
import com.tavall.hytale.resourcegame.dependency.interfaces.IResourceNodeVisualService;
import com.tavall.hytale.resourcegame.dependency.interfaces.IUiActionService;
import com.tavall.hytale.resourcegame.dependency.interfaces.IUiNavigator;
import com.tavall.hytale.resourcegame.domain.BuildingType;
import com.tavall.hytale.resourcegame.domain.BuildingMutationResult;
import com.tavall.hytale.resourcegame.domain.CastleBuildingData;
import com.tavall.hytale.resourcegame.domain.FocusedWorldTarget;
import com.tavall.hytale.resourcegame.domain.FocusedWorldTargetType;
import com.tavall.hytale.resourcegame.domain.PlacementResult;
import com.tavall.hytale.resourcegame.domain.PlayerGameState;
import com.tavall.hytale.resourcegame.domain.UiNavigationContext;
import com.tavall.hytale.resourcegame.domain.ResourceNodeData;
import com.tavall.hytale.resourcegame.domain.ResourceNodePillageResult;
import com.tavall.hytale.resourcegame.resources.ResourceType;
import com.tavall.hytale.resourcegame.services.BuildingPlacementPlanner;
import com.tavall.hytale.resourcegame.services.PlayerSession;
import com.tavall.hytale.resourcegame.services.WorldLabelService;
import com.tavall.hytale.resourcegame.tasks.AsyncTask;
import com.tavall.hytale.resourcegame.tasks.WorldTasks;
import com.tavall.hytale.resourcegame.world.BuildingPlacementStageStructureService;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Routes UI actions to services.
 */
public final class UiActionService implements IUiActionService, IDependencyInjectableConcrete {
    public static final String COMMAND_RETURN_SEPARATOR = "\u001F";

    private final IUiNavigator uiNavigator;
    private final IInteriorWorldService interiorWorldService;
    private final IPopulationService populationService;
    private final ICastleBuildingService buildingService;
    private final ICastleBuildingVisualService buildingVisualService;
    private final IPlayerSessionStore sessionStore;
    private final IPlayerGameStateService gameStateService;
    private final IResourceNodeService resourceNodeService;
    private final IResourceNodeVisualService resourceNodeVisualService;
    private final ICastleSpawnService castleSpawnService;
    private final ICastleSiteVisualService castleSiteVisualService;
    private final IPlacementModeService placementModeService;
    private final IFocusedWorldInteractionService focusedWorldInteractionService;
    private final ICustomEntitySpawnService customEntitySpawnService;
    private final BuildingPlacementPlanner buildingPlacementPlanner;
    private final BuildingPlacementStageStructureService stageStructureService;
    private final WorldLabelService worldLabelService;

    public UiActionService(
            IUiNavigator uiNavigator,
            IInteriorWorldService interiorWorldService,
            IPopulationService populationService,
            ICastleBuildingService buildingService,
            ICastleBuildingVisualService buildingVisualService,
            IPlayerSessionStore sessionStore,
            IPlayerGameStateService gameStateService,
            IResourceNodeService resourceNodeService,
            IResourceNodeVisualService resourceNodeVisualService,
            ICastleSpawnService castleSpawnService,
            ICastleSiteVisualService castleSiteVisualService,
            IPlacementModeService placementModeService,
            IFocusedWorldInteractionService focusedWorldInteractionService,
            ICustomEntitySpawnService customEntitySpawnService,
            BuildingPlacementPlanner buildingPlacementPlanner,
            BuildingPlacementStageStructureService stageStructureService,
            WorldLabelService worldLabelService
    ) {
        this.uiNavigator = Objects.requireNonNull(uiNavigator, "uiNavigator");
        this.interiorWorldService = Objects.requireNonNull(interiorWorldService, "interiorWorldService");
        this.populationService = Objects.requireNonNull(populationService, "populationService");
        this.buildingService = Objects.requireNonNull(buildingService, "buildingService");
        this.buildingVisualService = Objects.requireNonNull(buildingVisualService, "buildingVisualService");
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.gameStateService = Objects.requireNonNull(gameStateService, "gameStateService");
        this.resourceNodeService = Objects.requireNonNull(resourceNodeService, "resourceNodeService");
        this.resourceNodeVisualService = Objects.requireNonNull(resourceNodeVisualService, "resourceNodeVisualService");
        this.castleSpawnService = Objects.requireNonNull(castleSpawnService, "castleSpawnService");
        this.castleSiteVisualService = Objects.requireNonNull(castleSiteVisualService, "castleSiteVisualService");
        this.placementModeService = Objects.requireNonNull(placementModeService, "placementModeService");
        this.focusedWorldInteractionService = Objects.requireNonNull(focusedWorldInteractionService, "focusedWorldInteractionService");
        this.customEntitySpawnService = Objects.requireNonNull(customEntitySpawnService, "customEntitySpawnService");
        this.buildingPlacementPlanner = Objects.requireNonNull(buildingPlacementPlanner, "buildingPlacementPlanner");
        this.stageStructureService = Objects.requireNonNull(stageStructureService, "stageStructureService");
        this.worldLabelService = Objects.requireNonNull(worldLabelService, "worldLabelService");
    }

    public void handle(Player player, UiNavigationContext context, UiActionEventData eventData) {
        if (eventData == null || eventData.action() == null) {
            return;
        }
        String action = eventData.action();
        UUID playerId = context.playerId();
        PlayerSession session = sessionStore.get(playerId);
        PlayerGameState state = session == null ? null : session.gameState();
        if (UiActions.ENTER_INTERIOR.equals(action)) {
            interiorWorldService.enterInterior(player);
            return;
        }
        if (UiActions.EXIT_INTERIOR.equals(action)) {
            interiorWorldService.exitInterior(player);
            return;
        }
        if (UiActions.OPEN_CASTLE_INFO.equals(action) && state != null) {
            uiNavigator.open(UiPageType.CASTLE_INFO, player, context, state);
            return;
        }
        if (UiActions.OPEN_CITIZENS.equals(action) && state != null) {
            uiNavigator.open(UiPageType.CASTLE_CITIZENS, player, context, state);
            return;
        }
        if (UiActions.OPEN_TROOPS.equals(action) && state != null) {
            uiNavigator.open(UiPageType.CASTLE_TROOPS, player, context, state);
            return;
        }
        if (UiActions.OPEN_RESOURCES.equals(action) && state != null) {
            UiNavigationContext targetContext = context.selectedNodeId() == null
                    ? context
                    : context.clearFeedback().withSelectedNodeId(null);
            uiNavigator.open(UiPageType.CASTLE_RESOURCES, player, targetContext, state);
            return;
        }
        if (UiActions.OPEN_UPGRADES.equals(action) && state != null) {
            uiNavigator.open(UiPageType.CASTLE_UPGRADES, player, context, state);
            return;
        }
        if (UiActions.OPEN_BUILDINGS.equals(action) && state != null) {
            UiNavigationContext targetContext = context.clearFeedback()
                    .withSelectedNodeId(null)
                    .withSelectedBuildingId(null);
            uiNavigator.open(UiPageType.CASTLE_BUILDINGS, player, targetContext, state);
            return;
        }
        if (UiActions.OPEN_FARMSTEAD_MENU.equals(action) && state != null) {
            uiNavigator.open(UiPageType.FARMSTEAD_MENU, player, context.clearFeedback().withSelectedNodeId(null), state);
            return;
        }
        if (UiActions.OPEN_FARMSTEAD_UPGRADE.equals(action) && state != null) {
            openFarmsteadUpgrade(player, context, state);
            return;
        }
        if (UiActions.OPEN_CASTLE_MAIN.equals(action) && state != null) {
            uiNavigator.open(
                    UiPageType.CASTLE_MAIN,
                    player,
                    context.clearFeedback().withSelectedNodeId(null).withSelectedBuildingId(null),
                    state
            );
            return;
        }
        if (UiActions.OPEN_DEBUG.equals(action) && state != null) {
            uiNavigator.open(UiPageType.DEBUG_NAVIGATOR, player, context, state);
            return;
        }
        if (UiActions.OPEN_DEBUG_PLACEMENT.equals(action) && state != null) {
            uiNavigator.open(UiPageType.DEBUG_PLACEMENT, player, context, state);
            return;
        }
        if (UiActions.OPEN_DEBUG_INTERIOR.equals(action) && state != null) {
            uiNavigator.open(UiPageType.DEBUG_INTERIOR, player, context, state);
            return;
        }
        if (UiActions.OPEN_DEBUG_BUILDINGS.equals(action) && state != null) {
            uiNavigator.open(UiPageType.DEBUG_BUILDINGS, player, context, state);
            return;
        }
        if (UiActions.OPEN_DEBUG_WORLD.equals(action) && state != null) {
            uiNavigator.open(UiPageType.DEBUG_WORLD, player, context, state);
            return;
        }
        if (isPlacementAction(action) && state != null) {
            WorldTasks.executeSafe(player.getWorld(), "UiActionService." + action, () ->
                    handlePlacementAction(player, context, action, eventData.payload(), state));
            return;
        }
        if ((UiActions.BUILDING_PLACE.equals(action) || UiActions.BUILDING_STAGE.equals(action)) && state != null) {
            WorldTasks.executeSafe(player.getWorld(), "UiActionService." + action, () ->
                    handleBuildingPlacementAction(player, context, action, eventData.payload(), state));
            return;
        }
        if (isDebugInteriorAction(action)) {
            handleDebugInteriorAction(player, context, action, state);
            return;
        }
        if (UiActions.DEBUG_SCENE_REFRESH.equals(action) && state != null) {
            WorldTasks.executeSafe(player.getWorld(), "UiActionService.debugSceneRefresh", () ->
                    handleSceneRefresh(player, context, eventData.payload(), state));
            return;
        }
        if (UiActions.DEBUG_TUTORIAL_RESET.equals(action) && state != null) {
            handleTutorialReset(player, context, eventData.payload(), session, state);
            return;
        }
        if (UiActions.DEBUG_BUILDINGS_LIST.equals(action) && state != null) {
            openFeedback(player, context, UiPageType.DEBUG_BUILDINGS, state, buildingListFeedback(playerId, state));
            return;
        }
        if (UiActions.DEBUG_FOCUS.equals(action) && state != null) {
            openFeedback(player, context, UiPageType.DEBUG_BUILDINGS, state, focusFeedback(player));
            return;
        }
        if (UiActions.DEBUG_INTERACT.equals(action) && state != null) {
            handleFocusedInteract(player, context, state);
            return;
        }
        if (UiActions.DEBUG_NODES_LIST.equals(action) && state != null) {
            openFeedback(player, context, UiPageType.DEBUG_WORLD, state, nodeListFeedback(state));
            return;
        }
        if (UiActions.DEBUG_NODES_CLEAR.equals(action) && state != null) {
            handleNodesClear(player, context, state);
            return;
        }
        if (UiActions.DEBUG_HOLOGRAM_TEST.equals(action) && state != null) {
            WorldTasks.executeSafe(player.getWorld(), "UiActionService.debugHologramTest", () ->
                    handleHologramTest(player, context, state));
            return;
        }
        if (UiActions.DEBUG_ENTITY_SPAWN.equals(action) && state != null) {
            WorldTasks.executeSafe(player.getWorld(), "UiActionService.debugEntitySpawn", () ->
                    handleEntitySpawn(player, context, eventData.payload(), state));
            return;
        }
        if (UiActions.DEBUG_ENTITY_CLEAR.equals(action) && state != null) {
            WorldTasks.executeSafe(player.getWorld(), "UiActionService.debugEntityClear", () ->
                    handleEntityClear(player, context, state));
            return;
        }
        if (UiActions.RUN_COMMAND.equals(action)) {
            String payload = eventData.payload();
            if (payload == null || payload.isBlank()) {
                if (state != null) {
                    uiNavigator.open(UiPageType.DEBUG_NAVIGATOR, player, context.withFeedback("No command payload provided."), state);
                }
                return;
            }
            UiPageType returnPage = parseCommandReturnPage(payload);
            String commandPayload = parseCommandLine(payload);
            String commandLine = commandPayload.startsWith("/") ? commandPayload : ("/" + commandPayload);
            CommandManager.get().handleCommand(player, commandLine);
            if (state != null) {
                uiNavigator.open(returnPage == null ? UiPageType.DEBUG_NAVIGATOR : returnPage, player, context.withFeedback("Queued: " + commandLine), state);
            }
            return;
        }
        if (UiActions.CASTLE_ATTACK_PLACEHOLDER.equals(action) && state != null) {
            uiNavigator.open(UiPageType.CASTLE_INFO, player, context.withFeedback("Attack planning is a placeholder. Castle defense, scouting, and permission rules are tracked in TODO."), state);
            return;
        }
        if (UiActions.CASTLE_FRIEND_PLACEHOLDER.equals(action) && state != null) {
            uiNavigator.open(UiPageType.CASTLE_INFO, player, context.withFeedback("Friend access is a placeholder. This will become an invite and trust flow."), state);
            return;
        }
        if (UiActions.CASTLE_GUILD_PLACEHOLDER.equals(action) && state != null) {
            uiNavigator.open(UiPageType.CASTLE_INFO, player, context.withFeedback("Guild access is a placeholder. Guild roles and permission checks are deferred."), state);
            return;
        }
        if (UiActions.PROMOTE.equals(action)) {
            boolean promoted = populationService.promoteCitizen(playerId);
            PlayerSession updatedSession = sessionStore.get(playerId);
            if (updatedSession != null) {
                if (promoted) {
                    PlayerGameState updatedState = markUpgradeTutorialSeen(playerId, updatedSession.gameState());
                    updatedSession.updateGameState(updatedState);
                }
                String feedback = promoted
                        ? "Promotion complete."
                        : populationService.promoteActionState(updatedSession.gameState()).message();
                uiNavigator.open(UiPageType.CASTLE_UPGRADES, player, context.withFeedback(feedback), updatedSession.gameState());
            }
            return;
        }
        if (UiActions.DEMOTE.equals(action)) {
            boolean demoted = populationService.demoteTroop(playerId);
            PlayerSession updatedSession = sessionStore.get(playerId);
            if (updatedSession != null) {
                if (demoted) {
                    PlayerGameState updatedState = markUpgradeTutorialSeen(playerId, updatedSession.gameState());
                    updatedSession.updateGameState(updatedState);
                }
                String feedback = demoted
                        ? "Demotion complete."
                        : populationService.demoteActionState(updatedSession.gameState()).message();
                uiNavigator.open(UiPageType.CASTLE_UPGRADES, player, context.withFeedback(feedback), updatedSession.gameState());
            }
            return;
        }
        if (context.selectedNodeId() != null) {
            handleNodeAction(player, context, action, playerId, context.selectedNodeId());
            return;
        }
        if (context.selectedBuildingId() != null) {
            handleBuildingAction(player, context, action, playerId, context.selectedBuildingId());
        }
    }

    @Override
    public void handleClose(Player player, UiNavigationContext context) {
        if (context != null && context.playerId() != null) {
            uiNavigator.clearTrackedPage(context.playerId());
        }
    }

    public UpgradeActionState promoteActionState(PlayerGameState state) {
        return populationService.promoteActionState(state);
    }

    public UpgradeActionState demoteActionState(PlayerGameState state) {
        return populationService.demoteActionState(state);
    }

    public String promotionCostSummary(PlayerGameState state) {
        return populationService.promotionCostSummary(state);
    }

    public String upgradeTutorialMessage(PlayerGameState state) {
        if (gameStateService.isUpgradeTutorialPending(state)) {
            return "Step 1: confirm citizens and troops. Step 2: check the Food, Wood, and Iron cost. Step 3: promote once the route is ready.";
        }
        return "Tutorial complete: use this page to convert citizens when resources allow.";
    }

    public String interiorTutorialMessage(PlayerGameState state) {
        if (gameStateService.isInteriorTutorialPending(state) || gameStateService.isInteriorTourPending(state)) {
            return "Step 1: follow the tour markers. Step 2: inspect the citizen and troop anchors. Step 3: leave through the exit lane when you are done.";
        }
        return "Interior tutorial complete: citizen and troop anchors stay here while the upgrade pipeline grows.";
    }

    private PlayerGameState markUpgradeTutorialSeen(UUID playerId, PlayerGameState state) {
        Instant now = Instant.now();
        PlayerGameState updated = gameStateService.markUpgradeTutorialSeen(state, now);
        if (updated != state) {
            gameStateService.cacheState(playerId, updated);
            AsyncTask.runAsync(() -> gameStateService.persistState(updated, now));
        }
        return updated;
    }

    private boolean isPlacementAction(String action) {
        return UiActions.PLACEMENT_ARM_CASTLE.equals(action)
                || UiActions.PLACEMENT_ARM_NODE.equals(action)
                || UiActions.PLACEMENT_CONFIRM.equals(action)
                || UiActions.PLACEMENT_CANCEL.equals(action)
                || UiActions.PLACEMENT_MOVE.equals(action);
    }

    private boolean isDebugInteriorAction(String action) {
        return UiActions.DEBUG_INTERIOR_GENERATE.equals(action)
                || UiActions.DEBUG_INTERIOR_REBUILD.equals(action)
                || UiActions.DEBUG_INTERIOR_DELETE.equals(action)
                || UiActions.DEBUG_INTERIOR_MOVE.equals(action)
                || UiActions.DEBUG_INTERIOR_EXIT.equals(action);
    }

    private void handlePlacementAction(Player player, UiNavigationContext context, String action, String payload, PlayerGameState state) {
        PlacementResult result;
        if (UiActions.PLACEMENT_ARM_CASTLE.equals(action)) {
            placementModeService.armCastlePlacement(player);
            openFeedback(player, context, UiPageType.DEBUG_PLACEMENT, state, "Castle placement armed. Aim at a block, then confirm.");
            return;
        }
        if (UiActions.PLACEMENT_ARM_NODE.equals(action)) {
            ResourceType resourceType = parseResourceType(payload);
            if (resourceType == null) {
                openFeedback(player, context, UiPageType.DEBUG_PLACEMENT, state, "Unknown node type.");
                return;
            }
            placementModeService.armNodePlacement(player, resourceType);
            openFeedback(player, context, UiPageType.DEBUG_PLACEMENT, state, displayResourceType(resourceType) + " node placement armed.");
            return;
        }
        if (UiActions.PLACEMENT_CONFIRM.equals(action)) {
            result = placementModeService.confirmPlacementFromAim(player);
        } else if (UiActions.PLACEMENT_CANCEL.equals(action)) {
            result = placementModeService.cancelPlacement(player.getUuid());
        } else {
            Vector3i delta = parseMoveDelta(payload);
            if (delta == null) {
                openFeedback(player, context, UiPageType.DEBUG_PLACEMENT, state, "Invalid movement delta.");
                return;
            }
            result = placementModeService.moveStagedPlacement(player, delta);
        }
        openFeedback(player, context, UiPageType.DEBUG_PLACEMENT, result.updatedState() == null ? state : result.updatedState(), result.message());
    }

    private void handleBuildingPlacementAction(Player player, UiNavigationContext context, String action, String payload, PlayerGameState state) {
        UiPageType returnPage = parseReturnPage(payload, UiPageType.DEBUG_BUILDINGS);
        String buildingPayload = parsePayloadValue(payload);
        BuildingType buildingType = BuildingType.parse(buildingPayload);
        if (buildingType == null) {
            openFeedback(player, context, returnPage, state, "Unknown building type.");
            return;
        }
        PlayerSession session = sessionStore.get(context.playerId());
        if (session == null) {
            openFeedback(player, context, returnPage, state, "Building placement skipped: player session missing.");
            return;
        }
        if (UiActions.BUILDING_PLACE.equals(action)) {
            handleBuildingPlace(player, context, returnPage, buildingType, session.gameState());
            return;
        }
        handleBuildingStage(player, context, returnPage, buildingType, session);
    }

    private void handleBuildingPlace(Player player, UiNavigationContext context, UiPageType returnPage, BuildingType buildingType, PlayerGameState state) {
        placementModeService.armBuildingPlacement(player, buildingType);
        Vector3i currentBlock = currentStandingBlock(player);
        if (currentBlock == null) {
            openFeedback(player, context, returnPage, state, "Unable to resolve current standing block.");
            return;
        }
        PlacementResult result = placementModeService.confirmPlacement(player, currentBlock);
        openFeedback(player, context, returnPage, result.updatedState() == null ? state : result.updatedState(), result.message());
    }

    private void handleBuildingStage(
            Player player,
            UiNavigationContext context,
            UiPageType returnPage,
            BuildingType buildingType,
            PlayerSession session
    ) {
        String worldName = buildingPlacementPlanner.recommendedWorldName(session.playerId(), session.gameState(), buildingType);
        Vector3d anchor = buildingPlacementPlanner.recommendedPosition(session.playerId(), session.gameState(), buildingType);
        if (worldName == null || anchor == null) {
            openFeedback(player, context, returnPage, session.gameState(), "Unable to resolve a staging anchor for " + buildingType.displayName() + ".");
            return;
        }
        Optional<CastleBuildingData> existingBuilding = buildingService.resolveBuilding(session.gameState(), buildingType.shortKey());
        if (existingBuilding.isPresent()) {
            openFeedback(player, context, returnPage, session.gameState(), buildingType.displayName() + " already exists.");
            return;
        }
        var targetWorld = Universe.get().getWorld(worldName);
        if (targetWorld == null) {
            openFeedback(player, context, returnPage, session.gameState(), missingStageWorldMessage(buildingType));
            return;
        }
        if (player.getWorld() == null || !player.getWorld().getName().equals(targetWorld.getName())) {
            openFeedback(player, context, returnPage, session.gameState(), wrongAreaStageMessage(buildingType));
            return;
        }
        stageStructureService.ensureStagePad(targetWorld, anchor);
        Vector3i stagedTargetBlock = stagedTargetBlock(anchor);
        placementModeService.armBuildingPlacement(player, buildingType, stagedTargetBlock);
        openFeedback(player, context, returnPage, session.gameState(), stageMessage(buildingType, stagedTargetBlock));
    }

    private void handleDebugInteriorAction(Player player, UiNavigationContext context, String action, PlayerGameState state) {
        if (UiActions.DEBUG_INTERIOR_GENERATE.equals(action)) {
            interiorWorldService.generateInterior(player);
            return;
        }
        if (UiActions.DEBUG_INTERIOR_REBUILD.equals(action)) {
            interiorWorldService.rebuildInterior(player);
            return;
        }
        if (UiActions.DEBUG_INTERIOR_DELETE.equals(action)) {
            interiorWorldService.deleteInterior(player);
            return;
        }
        if (UiActions.DEBUG_INTERIOR_MOVE.equals(action)) {
            interiorWorldService.moveInterior(player);
            return;
        }
        if (UiActions.DEBUG_INTERIOR_EXIT.equals(action)) {
            interiorWorldService.exitInterior(player);
            return;
        }
        if (state != null) {
            openFeedback(player, context, UiPageType.DEBUG_INTERIOR, state, "Unknown interior action.");
        }
    }

    private void handleSceneRefresh(Player player, UiNavigationContext context, String payload, PlayerGameState state) {
        PlayerSession session = sessionStore.get(context.playerId());
        PlayerGameState currentState = session == null ? state : session.gameState();
        castleSpawnService.ensureCastleSpawned(player, currentState.castleLocation());
        castleSiteVisualService.refreshSite(context.playerId(), currentState);
        buildingVisualService.refreshBuildings(context.playerId(), currentState);
        resourceNodeVisualService.refreshNodes(context.playerId(), currentState);
        openFeedback(player, context, parseReturnPage(payload, UiPageType.DEBUG_INTERIOR), currentState, "Scene refreshed.");
    }

    private void handleTutorialReset(
            Player player,
            UiNavigationContext context,
            String payload,
            PlayerSession session,
            PlayerGameState state
    ) {
        if (session == null) {
            openFeedback(player, context, parseReturnPage(payload, UiPageType.DEBUG_INTERIOR), state, "Tutorial reset skipped: session missing.");
            return;
        }
        Instant now = Instant.now();
        PlayerGameState updatedState = gameStateService.resetOnboardingProgress(session.gameState(), now);
        session.updateGameState(updatedState);
        gameStateService.cacheState(session.playerId(), updatedState);
        AsyncTask.runAsync(() -> gameStateService.persistState(updatedState, now));
        openFeedback(player, context, parseReturnPage(payload, UiPageType.DEBUG_INTERIOR), updatedState, "Tutorial onboarding reset.");
    }

    private String buildingListFeedback(UUID playerId, PlayerGameState state) {
        List<CastleBuildingData> buildings = buildingService.listBuildings(state);
        if (buildings.isEmpty()) {
            return "No kingdom buildings placed yet.";
        }
        String firstSummary = buildingService.summaryLine(playerId, state, buildings.get(0), 1, Instant.now());
        return "Buildings: " + buildings.size() + " placed. First: " + firstSummary;
    }

    private String nodeListFeedback(PlayerGameState state) {
        List<ResourceNodeData> nodes = resourceNodeService.listNodes(state);
        if (nodes.isEmpty()) {
            return "No placed nodes. Use placement tools to arm a node preview.";
        }
        return "Nodes: " + nodes.size() + " placed. First: " + resourceNodeService.summaryLine(state, nodes.get(0), 1);
    }

    private String focusFeedback(Player player) {
        Optional<FocusedWorldTarget> target = focusedWorldInteractionService.resolve(player);
        return target.map(this::describeFocus).orElse("Focus: none.");
    }

    private void handleFocusedInteract(Player player, UiNavigationContext context, PlayerGameState state) {
        Optional<FocusedWorldTarget> target = focusedWorldInteractionService.interact(player);
        if (target.isEmpty()) {
            openFeedback(player, context, UiPageType.DEBUG_BUILDINGS, state, "No focused castle, node, or building in front of you.");
        }
    }

    private void handleNodesClear(Player player, UiNavigationContext context, PlayerGameState state) {
        PlayerGameState updatedState = resourceNodeService.clearNodes(context.playerId(), Instant.now());
        resourceNodeVisualService.refreshNodes(context.playerId(), updatedState);
        uiNavigator.refreshTrackedPage(context.playerId(), updatedState);
        openFeedback(player, context, UiPageType.DEBUG_WORLD, updatedState, "Cleared all placed nodes.");
    }

    private void handleHologramTest(Player player, UiNavigationContext context, PlayerGameState state) {
        if (player.getWorld() == null || player.getTransformComponent() == null || player.getTransformComponent().getPosition() == null) {
            openFeedback(player, context, UiPageType.DEBUG_WORLD, state, "Hologram test skipped: player world or position missing.");
            return;
        }
        Vector3d position = player.getTransformComponent().getPosition().add(0.0D, 2.6D, 0.0D);
        var refs = worldLabelService.spawnLabelStack(player.getWorld(), position, List.of("Test hologram", "Second line"));
        openFeedback(player, context, UiPageType.DEBUG_WORLD, state, "Spawned hologram stack with " + refs.size() + " label refs.");
    }

    private void handleEntitySpawn(Player player, UiNavigationContext context, String payload, PlayerGameState state) {
        String role = parsePayloadValue(payload);
        if (role.isBlank()) {
            openFeedback(player, context, UiPageType.DEBUG_WORLD, state, "Entity spawn skipped: role missing.");
            return;
        }
        customEntitySpawnService.spawn(player, role);
        openFeedback(player, context, UiPageType.DEBUG_WORLD, state, "Spawned entity role: " + role + ".");
    }

    private void handleEntityClear(Player player, UiNavigationContext context, PlayerGameState state) {
        customEntitySpawnService.clear(player);
        openFeedback(player, context, UiPageType.DEBUG_WORLD, state, "Custom entities cleared.");
    }

    private void openFeedback(Player player, UiNavigationContext context, UiPageType pageType, PlayerGameState fallbackState, String feedback) {
        PlayerSession session = sessionStore.get(context.playerId());
        PlayerGameState state = session == null ? fallbackState : session.gameState();
        if (state == null) {
            return;
        }
        uiNavigator.open(pageType, player, context.withFeedback(feedback == null || feedback.isBlank() ? "Done." : feedback), state);
    }

    private ResourceType parseResourceType(String token) {
        if (token == null) {
            return null;
        }
        return switch (token.toLowerCase(Locale.ROOT)) {
            case "food" -> ResourceType.FOOD;
            case "wood" -> ResourceType.WOOD;
            case "iron" -> ResourceType.IRON;
            default -> null;
        };
    }

    private String displayResourceType(ResourceType resourceType) {
        if (resourceType == null) {
            return "Unknown";
        }
        String lowerCaseName = resourceType.name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lowerCaseName.charAt(0)) + lowerCaseName.substring(1);
    }

    private Vector3i parseMoveDelta(String payload) {
        String value = parsePayloadValue(payload);
        if (value.isBlank()) {
            return null;
        }
        String[] parts = value.contains(",") ? value.split(",") : value.split("\\s+");
        if (parts.length != 2 && parts.length != 3) {
            return null;
        }
        try {
            int deltaX = Integer.parseInt(parts[0].trim());
            int deltaY = parts.length == 3 ? Integer.parseInt(parts[1].trim()) : 0;
            int deltaZ = Integer.parseInt(parts.length == 3 ? parts[2].trim() : parts[1].trim());
            return new Vector3i(deltaX, deltaY, deltaZ);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String describeFocus(FocusedWorldTarget target) {
        String label = target.type() == FocusedWorldTargetType.CASTLE
                ? "castle"
                : target.label().toLowerCase(Locale.ROOT);
        return "Focus: " + label
                + " | distance "
                + String.format(Locale.ROOT, "%.1f", target.distance())
                + " | alignment "
                + String.format(Locale.ROOT, "%.2f", target.alignmentScore());
    }

    private String stageMessage(BuildingType buildingType, Vector3i stagedTargetBlock) {
        return "Staged " + buildingType.displayName()
                + " at "
                + stagedTargetBlock.getX() + ", "
                + stagedTargetBlock.getY() + ", "
                + stagedTargetBlock.getZ()
                + ". Preview armed.";
    }

    private String missingStageWorldMessage(BuildingType buildingType) {
        return switch (buildingType.areaType()) {
            case CASTLE_INTERIOR -> "Interior world is not ready. Enter the interior before staging " + buildingType.displayName() + ".";
            case CASTLE_SURFACE -> "Castle world is not ready for staging " + buildingType.displayName() + ".";
        };
    }

    private String wrongAreaStageMessage(BuildingType buildingType) {
        return switch (buildingType.areaType()) {
            case CASTLE_INTERIOR -> "Enter the interior before staging " + buildingType.displayName() + ".";
            case CASTLE_SURFACE -> "Move to the castle surface before staging " + buildingType.displayName() + ".";
        };
    }

    private Vector3i stagedTargetBlock(Vector3d anchor) {
        return new Vector3i(
                (int) Math.floor(anchor.getX()),
                (int) Math.floor(anchor.getY()) - 1,
                (int) Math.floor(anchor.getZ())
        );
    }

    private Vector3i currentStandingBlock(Player player) {
        TransformComponent transform = player.getTransformComponent();
        if (transform == null || transform.getPosition() == null) {
            return null;
        }
        Vector3d position = transform.getPosition();
        return new Vector3i(
                (int) Math.floor(position.getX()),
                (int) Math.floor(position.getY()) - 1,
                (int) Math.floor(position.getZ())
        );
    }

    private void handleNodeAction(Player player, UiNavigationContext context, String action, UUID playerId, UUID nodeId) {
        PlayerSession session = sessionStore.get(playerId);
        if (session == null) {
            return;
        }
        Optional<ResourceNodeData> nodeOptional = resourceNodeService.findNode(session.gameState(), nodeId);
        if (nodeOptional.isEmpty()) {
            uiNavigator.open(UiPageType.CASTLE_RESOURCES, player, context.withFeedback("Node no longer exists.").withSelectedNodeId(null), session.gameState());
            return;
        }

        Instant now = Instant.now();
        PlayerGameState updatedState = switch (action) {
            case UiActions.NODE_ASSIGN_ONE -> resourceNodeService.addTroops(playerId, nodeId, 1, now);
            case UiActions.NODE_ASSIGN_THREE -> resourceNodeService.addTroops(playerId, nodeId, 3, now);
            case UiActions.NODE_ASSIGN_FIVE -> resourceNodeService.addTroops(playerId, nodeId, 5, now);
            case UiActions.NODE_ASSIGN_ALL -> resourceNodeService.assignTroops(
                    playerId,
                    nodeId,
                    nodeOptional.get().assignedTroops() + resourceNodeService.availableTroops(session.gameState()),
                    now
            );
            case UiActions.NODE_RECALL_ONE -> resourceNodeService.addTroops(playerId, nodeId, -1, now);
            case UiActions.NODE_RECALL_ALL -> resourceNodeService.assignTroops(playerId, nodeId, 0, now);
            default -> null;
        };
        if (UiActions.NODE_PILLAGE.equals(action)) {
            ResourceNodePillageResult pillageResult = resourceNodeService.pillageNode(playerId, nodeId, now);
            PlayerGameState pillagedState = pillageResult.state();
            if (pillagedState == null) {
                return;
            }
            if (pillageResult.changed()) {
                resourceNodeVisualService.refreshNodes(playerId, pillagedState);
            }
            uiNavigator.open(
                    UiPageType.RESOURCE_NODE_DETAIL,
                    player,
                    context.withFeedback(pillageResult.message()).withSelectedNodeId(nodeId),
                    pillagedState
            );
            return;
        }
        if (updatedState == null) {
            return;
        }
        resourceNodeVisualService.refreshNodes(playerId, updatedState);
        String feedback = describeNodeFeedback(action, updatedState, nodeId);
        uiNavigator.open(
                UiPageType.RESOURCE_NODE_DETAIL,
                player,
                context.withFeedback(feedback).withSelectedNodeId(nodeId),
                updatedState
        );
    }

    private void handleBuildingAction(Player player, UiNavigationContext context, String action, UUID playerId, UUID buildingId) {
        BuildingMutationResult mutationResult = switch (action) {
            case UiActions.BUILDING_START_UPGRADE -> buildingService.startUpgrade(playerId, buildingId, Instant.now());
            case UiActions.BUILDING_CANCEL_UPGRADE -> buildingService.cancelUpgrade(playerId, buildingId, Instant.now());
            default -> null;
        };
        if (mutationResult == null) {
            return;
        }
        PlayerGameState updatedState = mutationResult.state();
        if (updatedState == null) {
            return;
        }
        if (mutationResult.changed()) {
            buildingVisualService.refreshBuildings(playerId, updatedState);
        }
        uiNavigator.open(
                UiPageType.BUILDING_DETAIL,
                player,
                context.withFeedback(mutationResult.message()).withSelectedBuildingId(buildingId),
                updatedState
        );
    }

    private void openFarmsteadUpgrade(Player player, UiNavigationContext context, PlayerGameState state) {
        Optional<CastleBuildingData> farmstead = buildingService.resolveBuilding(state, BuildingType.FARMSTEAD.shortKey());
        if (farmstead.isEmpty()) {
            uiNavigator.open(
                    UiPageType.FARMSTEAD_MENU,
                    player,
                    context.withFeedback("Place a Farmstead before starting upgrades."),
                    state
            );
            return;
        }
        uiNavigator.open(
                UiPageType.BUILDING_DETAIL,
                player,
                context.clearFeedback().withSelectedBuildingId(farmstead.get().buildingId()),
                state
        );
    }

    private String describeNodeFeedback(String action, PlayerGameState updatedState, UUID nodeId) {
        Optional<ResourceNodeData> updatedNode = resourceNodeService.findNode(updatedState, nodeId);
        if (updatedNode.isEmpty()) {
            return "Node updated.";
        }
        String verb = switch (action) {
            case UiActions.NODE_ASSIGN_ONE, UiActions.NODE_ASSIGN_THREE, UiActions.NODE_ASSIGN_FIVE, UiActions.NODE_ASSIGN_ALL -> "Troops sent.";
            case UiActions.NODE_RECALL_ONE, UiActions.NODE_RECALL_ALL -> "Troops recalled.";
            case UiActions.NODE_PILLAGE -> "Node pillaged.";
            default -> "Node updated.";
        };
        return verb + " Assigned now: " + updatedNode.get().assignedTroops() + ".";
    }

    private UiPageType parseCommandReturnPage(String payload) {
        return parseReturnPage(payload, null);
    }

    private String parseCommandLine(String payload) {
        return parsePayloadValue(payload);
    }

    private UiPageType parseReturnPage(String payload, UiPageType defaultPage) {
        if (payload == null) {
            return defaultPage;
        }
        int separatorIndex = payload.indexOf(COMMAND_RETURN_SEPARATOR);
        if (separatorIndex <= 0) {
            return defaultPage;
        }
        try {
            return UiPageType.valueOf(payload.substring(0, separatorIndex));
        } catch (IllegalArgumentException ignored) {
            return defaultPage;
        }
    }

    private String parsePayloadValue(String payload) {
        if (payload == null) {
            return "";
        }
        int separatorIndex = payload.indexOf(COMMAND_RETURN_SEPARATOR);
        if (separatorIndex <= 0) {
            return payload;
        }
        return payload.substring(separatorIndex + COMMAND_RETURN_SEPARATOR.length());
    }
}
