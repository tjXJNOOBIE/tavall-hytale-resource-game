package org.tavall.control.dependency;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import org.tavall.control.castle.ICastleInteractionService;

/**
 * Test stub for castle interaction access.
 */
public final class TestCastleInteractionService implements ICastleInteractionService {
    @Override
    public void handleInteract(PlayerInteractEvent event) {
    }

    @Override
    public boolean isPlayerFocusingOwnedCastle(Player player) {
        return false;
    }

    @Override
    public void openCastleUi(Player player) {
    }
}
