package org.tavall.control.commands;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.castle.ICastleBuildingHandler;
import org.tavall.control.castle.ICastleBuildingVisualHandler;
import org.tavall.control.world.IFocusedWorldInteractionHandler;
import org.tavall.control.building.IPlacementModeHandler;
import org.tavall.control.player.IPlayerTeleportHandler;
import org.tavall.control.ui.IUiNavigator;
import org.tavall.control.domain.BuildingMutationResult;
import org.tavall.control.domain.BuildingType;
import org.tavall.control.domain.CastleBuildingData;
import org.tavall.control.domain.CastleBuildingSummary;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;
import org.tavall.control.building.BuildingPlacementPlanner;
import org.tavall.control.player.PlayerSession;
import org.tavall.control.ui.UiPageType;
import org.tavall.control.world.BuildingPlacementStageStructureHandler;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles `/kd buildings ...` command flows.
 */
public final class KingdomBuildingCommandSupport implements IDependencyInjectableConcrete {
    private final ICastleBuildingHandler buildingHandler;
    private final ICastleBuildingVisualHandler buildingVisualHandler;
    private final IUiNavigator uiNavigator;
    private final IPlayerTeleportHandler playerTeleportHandler;
    private final IPlacementModeHandler placementModeHandler;
    private final IFocusedWorldInteractionHandler focusedWorldInteractionHandler;
    private final BuildingPlacementPlanner buildingPlacementPlanner;
    private final BuildingPlacementStageStructureHandler stageStructureHandler;

    public KingdomBuildingCommandSupport(
            ICastleBuildingHandler buildingHandler,
            ICastleBuildingVisualHandler buildingVisualHandler,
            IUiNavigator uiNavigator,
            IPlayerTeleportHandler playerTeleportHandler,
            IPlacementModeHandler placementModeHandler,
            IFocusedWorldInteractionHandler focusedWorldInteractionHandler,
            BuildingPlacementPlanner buildingPlacementPlanner,
            BuildingPlacementStageStructureHandler stageStructureHandler
    ) {
        this.buildingHandler = Objects.requireNonNull(buildingHandler, "buildingHandler");
        this.buildingVisualHandler = Objects.requireNonNull(buildingVisualHandler, "buildingVisualHandler");
        this.uiNavigator = Objects.requireNonNull(uiNavigator, "uiNavigator");
        this.playerTeleportHandler = Objects.requireNonNull(playerTeleportHandler, "playerTeleportHandler");
        this.placementModeHandler = Objects.requireNonNull(placementModeHandler, "placementModeHandler");
        this.focusedWorldInteractionHandler = Objects.requireNonNull(focusedWorldInteractionHandler, "focusedWorldInteractionHandler");
        this.buildingPlacementPlanner = Objects.requireNonNull(buildingPlacementPlanner, "buildingPlacementPlanner");
        this.stageStructureHandler = Objects.requireNonNull(stageStructureHandler, "stageStructureHandler");
    }

    public void handle(CommandContext context, Player player, List<String> tokens, PlayerSession session) {
        if (tokens.size() < 2) {
            context.sendMessage(Message.raw("Usage: /kd buildings place|stage|spawn|list|status|select|align|goto|upgrade|cancel|finish|clear").color("yellow"));
            return;
        }
        String action = tokens.get(1).toLowerCase(Locale.ROOT);
        switch (action) {
            case "place" -> handlePlacement(context, player, tokens);
            case "stage" -> handleStage(context, player, tokens, session);
            case "spawn" -> handleSpawn(context, player, tokens, session);
            case "list" -> handleList(context, session);
            case "status" -> handleStatus(context, player, tokens, session);
            case "select" -> handleSelect(context, player, tokens, session);
            case "align", "goto" -> handleGoto(context, player, tokens, session);
            case "upgrade" -> handleUpgrade(context, player, tokens, session);
            case "cancel" -> handleCancel(context, player, tokens, session);
            case "finish" -> handleFinish(context, player, tokens, session);
            case "clear" -> handleClear(context, session);
            default -> context.sendMessage(Message.raw("Unknown buildings action.").color("red"));
        }
    }

    private void handlePlacement(CommandContext context, Player player, List<String> tokens) {
        if (tokens.size() < 3) {
            context.sendMessage(Message.raw("Usage: /kd buildings place <farmstead|lumber_mill|iron_works|barracks|workshop>").color("yellow"));
            return;
        }
        BuildingType buildingType = BuildingType.parse(tokens.get(2));
        if (buildingType == null) {
            context.sendMessage(Message.raw("Unknown building type.").color("red"));
            return;
        }
        placementModeHandler.armBuildingPlacement(player, buildingType);
        Vector3i currentBlock = currentStandingBlock(player);
        if (currentBlock == null) {
            context.sendMessage(Message.raw("Unable to resolve current standing block.").color("red"));
            return;
        }
        var result = placementModeHandler.confirmPlacement(player, currentBlock);
        context.sendMessage(Message.raw(result.message()).color(result.success() ? "green" : "red"));
    }

    private void handleStage(CommandContext context, Player player, List<String> tokens, PlayerSession session) {
        if (tokens.size() < 3) {
            context.sendMessage(Message.raw("Usage: /kd buildings stage <farmstead|lumber_mill|iron_works|barracks|workshop>").color("yellow"));
            return;
        }
        BuildingType buildingType = BuildingType.parse(tokens.get(2));
        if (buildingType == null) {
            context.sendMessage(Message.raw("Unknown building type.").color("red"));
            return;
        }
        String worldName = buildingPlacementPlanner.recommendedWorldName(session.playerId(), session.gameState(), buildingType);
        Vector3d anchor = buildingPlacementPlanner.recommendedPosition(session.playerId(), session.gameState(), buildingType);
        if (worldName == null || anchor == null) {
            context.sendMessage(Message.raw("Unable to resolve a staging anchor for " + buildingType.displayName() + ".").color("red"));
            return;
        }
        Optional<CastleBuildingData> existingBuilding = buildingHandler.resolveBuilding(session.gameState(), buildingType.shortKey());
        if (existingBuilding.isPresent()) {
            context.sendMessage(Message.raw(existingBuildingStageMessage(buildingType)).color("yellow"));
            return;
        }
        var targetWorld = Universe.get().getWorld(worldName);
        if (targetWorld == null) {
            context.sendMessage(Message.raw(missingStageWorldMessage(buildingType)).color("red"));
            return;
        }
        if (player.getWorld() == null || !player.getWorld().getName().equals(targetWorld.getName())) {
            context.sendMessage(Message.raw(wrongAreaStageMessage(buildingType)).color("red"));
            return;
        }
        stageStructureHandler.ensureStagePad(targetWorld, anchor);
        Vector3i stagedTargetBlock = stagedTargetBlock(anchor);
        placementModeHandler.armBuildingPlacement(player, buildingType, stagedTargetBlock);
        context.sendMessage(Message.raw(stageMessage(buildingType, stagedTargetBlock)).color("green"));
    }

    private void handleSpawn(CommandContext context, Player player, List<String> tokens, PlayerSession session) {
        if (tokens.size() < 4) {
            context.sendMessage(Message.raw("Usage: /kd buildings spawn <type> <level>").color("yellow"));
            return;
        }
        BuildingType buildingType = BuildingType.parse(tokens.get(2));
        if (buildingType == null) {
            context.sendMessage(Message.raw("Unknown building type.").color("red"));
            return;
        }
        int level;
        try {
            level = Integer.parseInt(tokens.get(3));
        } catch (NumberFormatException ex) {
            context.sendMessage(Message.raw("Level must be a whole number.").color("red"));
            return;
        }
        String worldName = buildingPlacementPlanner.recommendedWorldName(session.playerId(), session.gameState(), buildingType);
        Vector3d anchor = buildingPlacementPlanner.recommendedPosition(session.playerId(), session.gameState(), buildingType);
        if (worldName == null || anchor == null) {
            context.sendMessage(Message.raw("Unable to resolve a spawn anchor for " + buildingType.displayName() + ".").color("red"));
            return;
        }
        var targetWorld = Universe.get().getWorld(worldName);
        if (targetWorld == null) {
            context.sendMessage(Message.raw(missingStageWorldMessage(buildingType)).color("red"));
            return;
        }
        if (player.getWorld() == null || !player.getWorld().getName().equals(targetWorld.getName())) {
            context.sendMessage(Message.raw(wrongAreaStageMessage(buildingType)).color("red"));
            return;
        }
        BuildingMutationResult mutationResult = buildingHandler.spawnBuilding(session.playerId(), buildingType, level, worldName, anchor, Instant.now());
        if (mutationResult.state() != null && mutationResult.changed()) {
            buildingVisualHandler.refreshBuildings(session.playerId(), mutationResult.state());
            uiNavigator.refreshTrackedPage(session.playerId(), mutationResult.state());
        }
        context.sendMessage(Message.raw(mutationResult.message()).color(mutationResult.changed() ? "green" : "red"));
    }

    private void handleList(CommandContext context, PlayerSession session) {
        List<CastleBuildingData> buildings = buildingHandler.listBuildings(session.gameState());
        if (buildings.isEmpty()) {
            context.sendMessage(Message.raw("No kingdom buildings placed yet.").color("yellow"));
            return;
        }
        Instant now = Instant.now();
        for (int index = 0; index < buildings.size(); index++) {
            context.sendMessage(Message.raw(buildingHandler.summaryLine(session.playerId(), session.gameState(), buildings.get(index), index + 1, now)).color("yellow"));
        }
    }

    private void handleStatus(CommandContext context, Player player, List<String> tokens, PlayerSession session) {
        if (tokens.size() < 3) {
            context.sendMessage(Message.raw("Usage: /kd buildings status <index|type|id_prefix|focus>").color("yellow"));
            return;
        }
        Optional<CastleBuildingData> building = resolveBuilding(context, player, session, tokens.get(2));
        if (building.isEmpty()) {
            return;
        }
        sendSummary(context, session, building.get(), Instant.now());
    }

    private void handleSelect(CommandContext context, Player player, List<String> tokens, PlayerSession session) {
        if (tokens.size() < 3) {
            context.sendMessage(Message.raw("Usage: /kd buildings select <index|type|id_prefix|focus>").color("yellow"));
            return;
        }
        Optional<CastleBuildingData> building = resolveBuilding(context, player, session, tokens.get(2));
        if (building.isEmpty()) {
            return;
        }
        uiNavigator.open(
                UiPageType.BUILDING_DETAIL,
                player,
                new UiNavigationContext(player.getUuid(), player.getDisplayName()).withSelectedBuildingId(building.get().buildingId()),
                session.gameState()
        );
    }

    private void handleGoto(CommandContext context, Player player, List<String> tokens, PlayerSession session) {
        if (tokens.size() < 3) {
            context.sendMessage(Message.raw("Usage: /kd buildings goto <index|type|id_prefix|focus>").color("yellow"));
            return;
        }
        Optional<CastleBuildingData> building = resolveBuilding(context, player, session, tokens.get(2));
        if (building.isEmpty()) {
            return;
        }
        CastleBuildingSummary summary = buildingHandler.summary(session.playerId(), session.gameState(), building.get(), Instant.now());
        var world = Universe.get().getWorld(summary.worldName());
        if (world == null) {
            context.sendMessage(Message.raw("Target building world is not available.").color("red"));
            return;
        }
        Vector3d standing = playerTeleportHandler.standingPosition(player, new Vector3d(summary.worldX() + 2.5D, summary.worldY(), summary.worldZ()));
        playerTeleportHandler.teleport(player, world, standing);
        context.sendMessage(Message.raw("Aligned to " + building.get().buildingType().displayName() + ".").color("green"));
    }

    private void handleUpgrade(CommandContext context, Player player, List<String> tokens, PlayerSession session) {
        if (tokens.size() < 3) {
            context.sendMessage(Message.raw("Usage: /kd buildings upgrade <index|type|id_prefix|focus>").color("yellow"));
            return;
        }
        Optional<CastleBuildingData> building = resolveBuilding(context, player, session, tokens.get(2));
        if (building.isEmpty()) {
            return;
        }
        BuildingMutationResult mutationResult = buildingHandler.startUpgrade(session.playerId(), building.get().buildingId(), Instant.now());
        if (mutationResult.state() != null && mutationResult.changed()) {
            buildingVisualHandler.refreshBuildings(session.playerId(), mutationResult.state());
            uiNavigator.refreshTrackedPage(session.playerId(), mutationResult.state());
        }
        context.sendMessage(Message.raw(mutationResult.message()).color(mutationResult.changed() ? "green" : "red"));
    }

    private void handleCancel(CommandContext context, Player player, List<String> tokens, PlayerSession session) {
        if (tokens.size() < 3) {
            context.sendMessage(Message.raw("Usage: /kd buildings cancel <index|type|id_prefix|focus>").color("yellow"));
            return;
        }
        Optional<CastleBuildingData> building = resolveBuilding(context, player, session, tokens.get(2));
        if (building.isEmpty()) {
            return;
        }
        BuildingMutationResult mutationResult = buildingHandler.cancelUpgrade(session.playerId(), building.get().buildingId(), Instant.now());
        if (mutationResult.state() != null && mutationResult.changed()) {
            buildingVisualHandler.refreshBuildings(session.playerId(), mutationResult.state());
            uiNavigator.refreshTrackedPage(session.playerId(), mutationResult.state());
        }
        context.sendMessage(Message.raw(mutationResult.message()).color(mutationResult.changed() ? "green" : "red"));
    }

    private void handleFinish(CommandContext context, Player player, List<String> tokens, PlayerSession session) {
        if (tokens.size() < 3) {
            context.sendMessage(Message.raw("Usage: /kd buildings finish <index|type|id_prefix|focus>").color("yellow"));
            return;
        }
        Optional<CastleBuildingData> building = resolveBuilding(context, player, session, tokens.get(2));
        if (building.isEmpty()) {
            return;
        }
        BuildingMutationResult mutationResult = buildingHandler.forceComplete(session.playerId(), building.get().buildingId(), Instant.now());
        if (mutationResult.state() != null && mutationResult.changed()) {
            buildingVisualHandler.refreshBuildings(session.playerId(), mutationResult.state());
            uiNavigator.refreshTrackedPage(session.playerId(), mutationResult.state());
        }
        context.sendMessage(Message.raw(mutationResult.message()).color(mutationResult.changed() ? "green" : "red"));
    }

    private void handleClear(CommandContext context, PlayerSession session) {
        BuildingMutationResult mutationResult = buildingHandler.clearBuildings(session.playerId(), Instant.now());
        if (mutationResult.state() != null && mutationResult.changed()) {
            buildingVisualHandler.refreshBuildings(session.playerId(), mutationResult.state());
            uiNavigator.refreshTrackedPage(session.playerId(), mutationResult.state());
        }
        context.sendMessage(Message.raw(mutationResult.message()).color(mutationResult.changed() ? "green" : "red"));
    }

    private void sendSummary(CommandContext context, PlayerSession session, CastleBuildingData building, Instant now) {
        int index = buildingHandler.listBuildings(session.gameState()).indexOf(building) + 1;
        context.sendMessage(Message.raw(buildingHandler.summaryLine(session.playerId(), session.gameState(), building, index, now)).color("green"));
    }

    private Optional<CastleBuildingData> resolveBuilding(CommandContext context, Player player, PlayerSession session, String token) {
        if ("focus".equalsIgnoreCase(token)) {
            Optional<UUID> focusedBuildingId = focusedWorldInteractionHandler.focusedBuildingId(player);
            if (focusedBuildingId.isEmpty()) {
                context.sendMessage(Message.raw("No focused building in front of you.").color("red"));
                return Optional.empty();
            }
            return buildingHandler.findBuilding(session.gameState(), focusedBuildingId.get());
        }
        Optional<CastleBuildingData> building = buildingHandler.resolveBuilding(session.gameState(), token);
        if (building.isEmpty()) {
            context.sendMessage(Message.raw("Building not found.").color("red"));
        }
        return building;
    }

    private String stageMessage(BuildingType buildingType, Vector3i stagedTargetBlock) {
        return "Staged " + buildingType.displayName()
                + " at "
                + stagedTargetBlock.getX() + ", "
                + stagedTargetBlock.getY() + ", "
                + stagedTargetBlock.getZ()
                + ". Preview armed. Use /kd place confirm or /kd place cancel.";
    }

    private String missingStageWorldMessage(BuildingType buildingType) {
        if (buildingType.areaType() == org.tavall.control.domain.BuildingAreaType.CASTLE_INTERIOR) {
            return "Interior world is not ready. Enter /kd interior first, then stage " + buildingType.displayName() + ".";
        }
        return "Castle world is not ready for staging " + buildingType.displayName() + '.';
    }

    private String wrongAreaStageMessage(BuildingType buildingType) {
        if (buildingType.areaType() == org.tavall.control.domain.BuildingAreaType.CASTLE_INTERIOR) {
            return "Enter /kd interior before staging " + buildingType.displayName() + '.';
        }
        return "Move to the castle surface first with /kd castle goto, then stage " + buildingType.displayName() + '.';
    }

    private String existingBuildingStageMessage(BuildingType buildingType) {
        return buildingType.displayName() + " already exists. Use /kd buildings select "
                + buildingType.shortKey() + " or /kd buildings goto " + buildingType.shortKey() + '.';
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
}

