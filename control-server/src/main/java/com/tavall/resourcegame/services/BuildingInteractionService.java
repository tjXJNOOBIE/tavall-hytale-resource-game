package org.tavall.control.building;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.dependency.interfaces.IBuildingInteractionService;
import org.tavall.control.dependency.interfaces.ICastleBuildingVisualService;
import org.tavall.control.dependency.interfaces.IFocusedWorldInteractionService;
import org.tavall.control.dependency.interfaces.IPlayerSessionStore;
import org.tavall.control.dependency.interfaces.IUiNavigator;
import org.tavall.control.domain.UiNavigationContext;
import org.tavall.control.ui.UiPageType;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Opens the building detail UI when a player interacts with one of their building anchors.
 */
public final class BuildingInteractionService implements IBuildingInteractionService, IDependencyInjectableConcrete {
    private final IPlayerSessionStore sessionStore;
    private final ICastleBuildingVisualService buildingVisualService;
    private final IFocusedWorldInteractionService focusedWorldInteractionService;
    private final IUiNavigator uiNavigator;

    public BuildingInteractionService(
            IPlayerSessionStore sessionStore,
            ICastleBuildingVisualService buildingVisualService,
            IFocusedWorldInteractionService focusedWorldInteractionService,
            IUiNavigator uiNavigator
    ) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.buildingVisualService = Objects.requireNonNull(buildingVisualService, "buildingVisualService");
        this.focusedWorldInteractionService = Objects.requireNonNull(focusedWorldInteractionService, "focusedWorldInteractionService");
        this.uiNavigator = Objects.requireNonNull(uiNavigator, "uiNavigator");
    }

    @Override
    public void handleInteract(PlayerInteractEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }
        openFromTarget(event.getPlayer(), event.getTargetRef());
    }

    @Override
    public boolean openFromTarget(Player player, Ref<EntityStore> targetRef) {
        if (player == null) {
            return false;
        }
        UUID playerId = player.getUuid();
        PlayerSession session = sessionStore.get(playerId);
        if (session == null) {
            return false;
        }
        Optional<UUID> buildingId = buildingVisualService.findBuildingId(playerId, targetRef);
        if (buildingId.isEmpty()) {
            buildingId = focusedWorldInteractionService.focusedBuildingId(player);
        }
        if (buildingId.isEmpty()) {
            return false;
        }
        uiNavigator.open(
                UiPageType.BUILDING_DETAIL,
                player,
                new UiNavigationContext(playerId, player.getDisplayName()).withSelectedBuildingId(buildingId.get()),
                session.gameState()
        );
        return true;
    }
}
