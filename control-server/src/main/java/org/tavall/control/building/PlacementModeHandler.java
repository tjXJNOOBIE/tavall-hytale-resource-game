package org.tavall.control.building;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.castle.ICastleBuildingHandler;
import org.tavall.control.castle.ICastleBuildingVisualHandler;
import org.tavall.control.castle.ICastlePlacementHandler;
import org.tavall.control.building.IPlacementModeHandler;
import org.tavall.control.building.IPlacementPreviewHandler;
import org.tavall.control.player.IPlayerSessionStore;
import org.tavall.control.resource.IResourceNodeHandler;
import org.tavall.control.resource.IResourceNodeVisualHandler;
import org.tavall.control.domain.BuildingMutationResult;
import org.tavall.control.domain.BuildingType;
import org.tavall.control.domain.CastleLocationData;
import org.tavall.control.domain.PlacementModeType;
import org.tavall.control.domain.PlacementRequest;
import org.tavall.control.domain.PlacementResult;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.ResourceNodeData;
import org.tavall.control.resources.ResourceType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Duration;

/**
 * Maintains armed placement state and finalizes placements from world interactions.
 */
public final class PlacementModeHandler implements IPlacementModeHandler, IDependencyInjectableConcrete {
    private static final double TARGET_DISTANCE = 12.0D;
    private static final Duration PROMPT_SUPPRESSION_WINDOW = Duration.ofSeconds(4);

    private final IPlayerSessionStore sessionStore;
    private final IPlacementPreviewHandler previewHandler;
    private final ICastleBuildingHandler buildingHandler;
    private final ICastleBuildingVisualHandler buildingVisualHandler;
    private final ICastlePlacementHandler castlePlacementHandler;
    private final IResourceNodeHandler resourceNodeHandler;
    private final IResourceNodeVisualHandler resourceNodeVisualHandler;
    private final Map<UUID, PlacementRequest> activeRequests = new ConcurrentHashMap<>();
    private final Map<UUID, Instant> recentPlacementActivity = new ConcurrentHashMap<>();

    public PlacementModeHandler(
            IPlayerSessionStore sessionStore,
            IPlacementPreviewHandler previewHandler,
            ICastleBuildingHandler buildingHandler,
            ICastleBuildingVisualHandler buildingVisualHandler,
            ICastlePlacementHandler castlePlacementHandler,
            IResourceNodeHandler resourceNodeHandler,
            IResourceNodeVisualHandler resourceNodeVisualHandler
    ) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.previewHandler = Objects.requireNonNull(previewHandler, "previewHandler");
        this.buildingHandler = Objects.requireNonNull(buildingHandler, "buildingHandler");
        this.buildingVisualHandler = Objects.requireNonNull(buildingVisualHandler, "buildingVisualHandler");
        this.castlePlacementHandler = Objects.requireNonNull(castlePlacementHandler, "castlePlacementHandler");
        this.resourceNodeHandler = Objects.requireNonNull(resourceNodeHandler, "resourceNodeHandler");
        this.resourceNodeVisualHandler = Objects.requireNonNull(resourceNodeVisualHandler, "resourceNodeVisualHandler");
    }

    @Override
    public PlacementRequest armCastlePlacement(Player player) {
        PlacementRequest request = new PlacementRequest(
                PlacementModeType.CASTLE,
                null,
                null,
                player.getWorld().getName(),
                Instant.now()
        );
        activeRequests.put(player.getUuid(), request);
        refreshPreview(player);
        return request;
    }

    @Override
    public PlacementRequest armNodePlacement(Player player, ResourceType resourceType) {
        PlacementRequest request = new PlacementRequest(
                PlacementModeType.RESOURCE_NODE,
                resourceType,
                null,
                player.getWorld().getName(),
                Instant.now()
        );
        activeRequests.put(player.getUuid(), request);
        refreshPreview(player);
        return request;
    }

    @Override
    public PlacementRequest armBuildingPlacement(Player player, BuildingType buildingType) {
        return armBuildingPlacement(player, buildingType, null);
    }

    @Override
    public PlacementRequest armBuildingPlacement(Player player, BuildingType buildingType, Vector3i stagedTargetBlock) {
        PlacementRequest request = new PlacementRequest(
                PlacementModeType.BUILDING,
                null,
                buildingType,
                player.getWorld().getName(),
                Instant.now(),
                stagedTargetBlock
        );
        activeRequests.put(player.getUuid(), request);
        refreshPreview(player);
        return request;
    }

    @Override
    public Optional<PlacementRequest> activePlacement(UUID playerId) {
        return Optional.ofNullable(activeRequests.get(playerId));
    }

    @Override
    public boolean hasActivePlacement(UUID playerId) {
        return activeRequests.containsKey(playerId);
    }

    @Override
    public PlacementResult cancelPlacement(UUID playerId) {
        PlacementRequest removed = activeRequests.remove(playerId);
        previewHandler.clearPreview(playerId);
        if (removed == null) {
            return PlacementResult.failure("No active placement.");
        }
        recentPlacementActivity.put(playerId, Instant.now());
        return PlacementResult.success(removed.summary() + " cancelled.", null);
    }

    @Override
    public PlacementResult refreshPreview(Player player) {
        PlacementRequest request = activeRequests.get(player.getUuid());
        if (request == null) {
            return PlacementResult.failure("No active placement.");
        }
        Vector3i targetBlock = request.stagedTargetBlock() == null ? resolveTargetBlock(player) : request.stagedTargetBlock();
        if (targetBlock == null) {
            previewHandler.clearPreview(player.getUuid());
            return PlacementResult.failure("Look at a solid block within 12 blocks to preview placement.");
        }
        previewHandler.showPreview(player, request, targetBlock);
        return PlacementResult.success(request.summary() + " preview: " + targetBlock.getX() + ", " + targetBlock.getY() + ", " + targetBlock.getZ(), null);
    }

    @Override
    public PlacementResult moveStagedPlacement(Player player, Vector3i delta) {
        if (player == null || delta == null) {
            return PlacementResult.failure("Placement move data is missing.");
        }
        UUID playerId = player.getUuid();
        PlacementRequest request = activeRequests.get(playerId);
        if (request == null) {
            return PlacementResult.failure("No active placement.");
        }
        Vector3i staged = request.stagedTargetBlock();
        if (staged == null) {
            return PlacementResult.failure("Move is only available for staged placements.");
        }
        Vector3i moved = new Vector3i(
                staged.getX() + delta.getX(),
                staged.getY() + delta.getY(),
                staged.getZ() + delta.getZ()
        );
        PlacementRequest updatedRequest = new PlacementRequest(
                request.modeType(),
                request.resourceType(),
                request.buildingType(),
                request.armedWorldName(),
                request.armedAt(),
                moved
        );
        activeRequests.put(playerId, updatedRequest);
        previewHandler.showPreview(player, updatedRequest, moved);
        recentPlacementActivity.put(playerId, Instant.now());
        return PlacementResult.success("Moved placement to " + moved.getX() + ", " + moved.getY() + ", " + moved.getZ() + ".", null);
    }

    @Override
    public PlacementResult confirmPlacement(Player player, Vector3i targetBlock) {
        PlacementRequest request = activeRequests.get(player.getUuid());
        if (request == null) {
            return PlacementResult.ignored();
        }
        if (targetBlock == null) {
            return PlacementResult.failure("Placement blocked: click a solid block.");
        }
        Instant now = Instant.now();
        PlayerGameState updatedState;
        String successMessage;
        if (request.modeType() == PlacementModeType.CASTLE) {
            updatedState = castlePlacementHandler.placeCastle(player.getUuid(), toLocation(player, targetBlock), now);
            successMessage = successMessage(request, updatedState);
        } else if (request.modeType() == PlacementModeType.BUILDING) {
            BuildingMutationResult mutationResult = buildingHandler.placeBuilding(
                    player.getUuid(),
                    request.buildingType(),
                    player.getWorld().getName(),
                    toPosition(targetBlock),
                    now
            );
            updatedState = mutationResult.state();
            if (!mutationResult.changed()) {
                return PlacementResult.failure(mutationResult.message());
            }
            if (updatedState != null) {
                buildingVisualHandler.refreshBuildings(player.getUuid(), updatedState);
            }
            successMessage = mutationResult.message();
        } else {
            updatedState = resourceNodeHandler.placeNode(
                    player.getUuid(),
                    request.resourceType(),
                    player.getWorld().getName(),
                    toPosition(targetBlock),
                    now
            );
            if (updatedState != null) {
                resourceNodeVisualHandler.refreshNodes(player.getUuid(), updatedState);
            }
            successMessage = successMessage(request, updatedState);
        }
        if (updatedState == null) {
            return PlacementResult.failure("Placement failed.");
        }
        previewHandler.clearPreview(player.getUuid());
        activeRequests.remove(player.getUuid());
        recentPlacementActivity.put(player.getUuid(), now);
        return PlacementResult.success(successMessage, updatedState);
    }

    @Override
    public PlacementResult confirmPlacementFromAim(Player player) {
        PlacementRequest request = activeRequests.get(player.getUuid());
        if (request == null) {
            return PlacementResult.failure("No active placement.");
        }
        Vector3i targetBlock = request.stagedTargetBlock() == null ? resolveTargetBlock(player) : request.stagedTargetBlock();
        if (targetBlock == null) {
            return PlacementResult.failure("Look at a solid block within 12 blocks before confirming placement.");
        }
        return confirmPlacement(player, targetBlock);
    }

    @Override
    public boolean shouldSuppressPrompts(UUID playerId, Instant now) {
        if (hasActivePlacement(playerId)) {
            return true;
        }
        Instant lastActivity = recentPlacementActivity.get(playerId);
        if (lastActivity == null) {
            return false;
        }
        if (lastActivity.plus(PROMPT_SUPPRESSION_WINDOW).isAfter(now)) {
            return true;
        }
        recentPlacementActivity.remove(playerId, lastActivity);
        return false;
    }

    private Vector3i resolveTargetBlock(Player player) {
        Ref<EntityStore> playerRef = player.getPlayerRef() == null ? null : player.getPlayerRef().getReference();
        if (playerRef == null || !playerRef.isValid()) {
            return null;
        }
        Store<EntityStore> store = playerRef.getStore();
        return TargetUtil.getTargetBlock(playerRef, TARGET_DISTANCE, store);
    }

    private CastleLocationData toLocation(Player player, Vector3i targetBlock) {
        Vector3d position = toPosition(targetBlock);
        return new CastleLocationData(player.getWorld().getName(), position.getX(), position.getY(), position.getZ());
    }

    private Vector3d toPosition(Vector3i targetBlock) {
        return new Vector3d(targetBlock.getX() + 0.5D, targetBlock.getY() + 1.0D, targetBlock.getZ() + 0.5D);
    }

    private String successMessage(PlacementRequest request, PlayerGameState updatedState) {
        if (request.modeType() == PlacementModeType.CASTLE) {
            CastleLocationData location = updatedState.castleLocation();
            return "Castle placed at " + (int) location.x() + ", " + (int) location.y() + ", " + (int) location.z() + ".";
        }
        if (request.modeType() == PlacementModeType.BUILDING) {
            return request.buildingType() == null
                    ? "Building construction started."
                    : request.buildingType().displayName() + " construction started.";
        }
        List<ResourceNodeData> nodes = resourceNodeHandler.listNodes(updatedState);
        ResourceNodeData latestNode = nodes.isEmpty() ? null : nodes.getLast();
        if (latestNode == null) {
            return request.summary() + " confirmed.";
        }
        return request.resourceType() + " node placed #" + nodes.size() + " " + latestNode.nodeId().toString().substring(0, 8) + ".";
    }
}

