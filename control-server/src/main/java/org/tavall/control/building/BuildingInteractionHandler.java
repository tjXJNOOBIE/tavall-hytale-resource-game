package org.tavall.control.building;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.building.IBuildingInteractionHandler;
import org.tavall.control.castle.ICastleBuildingVisualHandler;
import org.tavall.control.world.IFocusedWorldInteractionHandler;
import org.tavall.control.player.IPlayerSessionStore;
import org.tavall.control.api.UIData;
import org.tavall.control.domain.UiNavigationContext;
import org.tavall.minecraft.framework.game.ui.UiScreenKey;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Opens the building detail UI when a player interacts with one of their building anchors.
 */
public final class BuildingInteractionHandler implements IBuildingInteractionHandler, IDependencyInjectableConcrete {
    private final IPlayerSessionStore sessionStore;
    private final ICastleBuildingVisualHandler buildingVisualHandler;
    private final IFocusedWorldInteractionHandler focusedWorldInteractionHandler;
    private final UIData uiNavigator;

    public BuildingInteractionHandler(
            IPlayerSessionStore sessionStore,
            ICastleBuildingVisualHandler buildingVisualHandler,
            IFocusedWorldInteractionHandler focusedWorldInteractionHandler,
            UIData uiNavigator
    ) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.buildingVisualHandler = Objects.requireNonNull(buildingVisualHandler, "buildingVisualHandler");
        this.focusedWorldInteractionHandler = Objects.requireNonNull(focusedWorldInteractionHandler, "focusedWorldInteractionHandler");
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
        Optional<UUID> buildingId = buildingVisualHandler.findBuildingId(playerId, targetRef);
        if (buildingId.isEmpty()) {
            buildingId = focusedWorldInteractionHandler.focusedBuildingId(player);
        }
        if (buildingId.isEmpty()) {
            return false;
        }
        uiNavigator.open(
                UiScreenKey.BUILDING_DETAIL,
                player,
                new UiNavigationContext(playerId, player.getDisplayName()).withSelectedBuildingId(buildingId.get()),
                session.gameState()
        );
        return true;
    }
}
