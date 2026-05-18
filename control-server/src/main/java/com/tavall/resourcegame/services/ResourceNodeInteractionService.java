package org.tavall.control.services;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.dependency.interfaces.IFocusedWorldInteractionService;
import org.tavall.control.dependency.interfaces.IPlayerSessionStore;
import org.tavall.control.dependency.interfaces.IResourceNodeInteractionService;
import org.tavall.control.dependency.interfaces.IResourceNodeVisualService;
import org.tavall.control.dependency.interfaces.IUiNavigator;
import org.tavall.control.domain.UiNavigationContext;
import org.tavall.control.ui.UiPageType;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Opens the node detail UI when a player interacts with one of their node anchors.
 */
public final class ResourceNodeInteractionService implements IResourceNodeInteractionService, IDependencyInjectableConcrete {
    private final IPlayerSessionStore sessionStore;
    private final IResourceNodeVisualService resourceNodeVisualService;
    private final IFocusedWorldInteractionService focusedWorldInteractionService;
    private final IUiNavigator uiNavigator;

    public ResourceNodeInteractionService(
            IPlayerSessionStore sessionStore,
            IResourceNodeVisualService resourceNodeVisualService,
            IFocusedWorldInteractionService focusedWorldInteractionService,
            IUiNavigator uiNavigator
    ) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.resourceNodeVisualService = Objects.requireNonNull(resourceNodeVisualService, "resourceNodeVisualService");
        this.focusedWorldInteractionService = Objects.requireNonNull(focusedWorldInteractionService, "focusedWorldInteractionService");
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
        Optional<UUID> nodeId = resourceNodeVisualService.findNodeId(playerId, event.getTargetRef());
        if (nodeId.isEmpty()) {
            nodeId = focusedWorldInteractionService.focusedNodeId(player);
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
