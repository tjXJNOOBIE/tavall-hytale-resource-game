package org.tavall.control.building;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.control.building.IPlacementInteractionHandler;
import org.tavall.control.building.IPlacementModeHandler;
import org.tavall.control.domain.PlacementResult;

import java.util.Objects;

/**
 * Consumes world interaction while placement mode is armed.
 */
public final class PlacementInteractionHandler implements IPlacementInteractionHandler, IDependencyInjectableConcrete {
    private final IPlacementModeHandler placementModeHandler;

    public PlacementInteractionHandler(IPlacementModeHandler placementModeHandler) {
        this.placementModeHandler = Objects.requireNonNull(placementModeHandler, "placementModeHandler");
    }

    @Override
    public void handleInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        PlacementResult result = placementModeHandler.confirmPlacement(event.getPlayer(), event.getTargetBlock());
        if (!result.handled()) {
            return;
        }
        event.setCancelled(true);
        if (!result.message().isBlank()) {
            event.getPlayer().sendMessage(Message.raw(result.message()).color(result.success() ? "green" : "red"));
        }
    }
}

