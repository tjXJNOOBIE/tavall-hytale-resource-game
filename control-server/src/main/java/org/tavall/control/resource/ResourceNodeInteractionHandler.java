package org.tavall.control.resource;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.world.IFocusedWorldInteractionHandler;
import org.tavall.control.player.IPlayerSessionStore;
import org.tavall.control.resource.IResourceNodeInteractionHandler;
import org.tavall.control.resource.IResourceNodeVisualHandler;
import org.tavall.control.ui.IUiNavigator;
import org.tavall.control.domain.UiNavigationContext;
import org.tavall.control.ui.UiPageType;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Opens the node detail UI when a player interacts with one of their node anchors.
 */
public final class ResourceNodeInteractionHandler implements IResourceNodeInteractionHandler, IDependencyInjectableConcrete {
    private final IPlayerSessionStore sessionStore;
    private final IResourceNodeVisualHandler resourceNodeVisualHandler;
    private final IFocusedWorldInteractionHandler focusedWorldInteractionHandler;
    private final IUiNavigator uiNavigator;

    public ResourceNodeInteractionHandler(
            IPlayerSessionStore sessionStore,
            IResourceNodeVisualHandler resourceNodeVisualHandler,
            IFocusedWorldInteractionHandler focusedWorldInteractionHandler,
            IUiNavigator uiNavigator
    ) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.resourceNodeVisualHandler = Objects.requireNonNull(resourceNodeVisualHandler, "resourceNodeVisualHandler");
        this.focusedWorldInteractionHandler = Objects.requireNonNull(focusedWorldInteractionHandler, "focusedWorldInteractionHandler");
        this.uiNavigator = Objects.requireNonNull(uiNavigator, "uiNavigator");
    }

    @Override
    public void handleInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        UUID playerId = player.getUuid();
        PlayerSession session = sessionStore.get(playerId);
        if (session == null) {
            return;
        }
        Optional<UUID> nodeId = resourceNodeVisualHandler.findNodeId(playerId, event.getTargetRef());
        if (nodeId.isEmpty()) {
            nodeId = focusedWorldInteractionHandler.focusedNodeId(player);
        }
        if (nodeId.isEmpty()) {
            return;
        }
        uiNavigator.open(
                UiPageType.RESOURCE_NODE_DETAIL,
                player,
                new UiNavigationContext(playerId, player.getDisplayName()).withSelectedNodeId(nodeId.get()),
                session.gameState()
        );
    }
}

