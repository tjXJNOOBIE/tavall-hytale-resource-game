package org.tavall.control.world;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.castle.ICastleBuildingService;
import org.tavall.control.castle.ICastleInteractionService;
import org.tavall.control.world.IFocusedWorldInteractionService;
import org.tavall.control.world.IFocusedWorldOverrideService;
import org.tavall.control.player.IPlayerSessionStore;
import org.tavall.control.resource.IResourceNodeService;
import org.tavall.control.ui.IUiNavigator;
import org.tavall.control.domain.FocusedWorldTarget;
import org.tavall.control.domain.FocusedWorldTargetType;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;
import org.tavall.control.ui.UiPageType;
import org.tavall.control.world.VectorMath;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides a single explicit interaction path for focused castle and node targets.
 */
public final class FocusedWorldInteractionService implements IFocusedWorldInteractionService, IDependencyInjectableConcrete {
    private final IPlayerSessionStore sessionStore;
    private final ICastleBuildingService buildingService;
    private final ICastleInteractionService castleInteractionService;
    private final IFocusedWorldOverrideService focusedWorldOverrideService;
    private final IResourceNodeService resourceNodeService;
    private final IUiNavigator uiNavigator;
    private final FocusedWorldTargetPlanner planner;

    public FocusedWorldInteractionService(
            IPlayerSessionStore sessionStore,
            ICastleBuildingService buildingService,
            ICastleInteractionService castleInteractionService,
            IFocusedWorldOverrideService focusedWorldOverrideService,
            IResourceNodeService resourceNodeService,
            IUiNavigator uiNavigator,
            FocusedWorldTargetPlanner planner
    ) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.buildingService = Objects.requireNonNull(buildingService, "buildingService");
        this.castleInteractionService = Objects.requireNonNull(castleInteractionService, "castleInteractionService");
        this.focusedWorldOverrideService = Objects.requireNonNull(focusedWorldOverrideService, "focusedWorldOverrideService");
        this.resourceNodeService = Objects.requireNonNull(resourceNodeService, "resourceNodeService");
        this.uiNavigator = Objects.requireNonNull(uiNavigator, "uiNavigator");
        this.planner = Objects.requireNonNull(planner, "planner");
    }

    @Override
    public Optional<FocusedWorldTarget> resolve(Player player) {
        PlayerSession session = sessionStore.get(player.getUuid());
        if (session == null) {
            return Optional.empty();
        }
        Optional<FocusedWorldTarget> overrideTarget = focusedWorldOverrideService.peek(player.getUuid());
        if (overrideTarget.isPresent()) {
            return overrideTarget;
        }
        TransformComponent transform = player.getTransformComponent();
        if (transform == null || player.getWorld() == null) {
            return Optional.empty();
        }
        Vector3d playerPosition = transform.getPosition();
        Vector3f rotation = transform.getRotation();
        Vector3d lookVector = VectorMath.lookVector(rotation);
        PlayerGameState state = session.gameState();
        return planner.resolve(
                player.getWorld().getName(),
                playerPosition,
                lookVector,
                state.castleLocation(),
                resourceNodeService.listNodes(state),
                buildingService.listBuildings(state).stream()
                        .map(building -> buildingService.summary(player.getUuid(), state, building, java.time.Instant.now()))
                        .toList()
        );
    }

    @Override
    public Optional<UUID> focusedNodeId(Player player) {
        return resolve(player)
                .filter(target -> target.type() == FocusedWorldTargetType.RESOURCE_NODE)
                .map(FocusedWorldTarget::nodeId);
    }

    @Override
    public Optional<UUID> focusedBuildingId(Player player) {
        return resolve(player)
                .filter(target -> target.type() == FocusedWorldTargetType.BUILDING)
                .map(FocusedWorldTarget::buildingId);
    }

    @Override
    public Optional<FocusedWorldTarget> interact(Player player) {
        Optional<FocusedWorldTarget> target = resolve(player);
        if (target.isEmpty()) {
            return Optional.empty();
        }
        PlayerSession session = sessionStore.get(player.getUuid());
        if (session == null) {
            return Optional.empty();
        }
        Optional<FocusedWorldTarget> overrideTarget = focusedWorldOverrideService.consume(player.getUuid());
        FocusedWorldTarget focusedTarget = overrideTarget.orElseGet(target::get);
        if (focusedTarget.type() == FocusedWorldTargetType.CASTLE) {
            castleInteractionService.openCastleUi(player);
            return Optional.of(focusedTarget);
        }
        if (focusedTarget.type() == FocusedWorldTargetType.BUILDING) {
            uiNavigator.open(
                    UiPageType.BUILDING_DETAIL,
                    player,
                    new UiNavigationContext(player.getUuid(), player.getDisplayName()).withSelectedBuildingId(focusedTarget.buildingId()),
                    session.gameState()
            );
            return Optional.of(focusedTarget);
        }
        uiNavigator.open(
                UiPageType.RESOURCE_NODE_DETAIL,
                player,
                new UiNavigationContext(player.getUuid(), player.getDisplayName()).withSelectedNodeId(focusedTarget.nodeId()),
                session.gameState()
        );
        return Optional.of(focusedTarget);
    }
}

