package org.tavall.control.dependency;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import org.tavall.control.player.IPlayerDataHandler;
import org.tavall.control.player.PlayerSession;

import java.util.concurrent.CompletableFuture;

/**
 * Test stub for player data access.
 */
public final class TestPlayerDataHandler implements IPlayerDataHandler {
    @Override
    public void handlePlayerReady(PlayerReadyEvent event) {
    }

    @Override
    public void handlePlayerDisconnect(PlayerDisconnectEvent event) {
    }

    @Override
    public CompletableFuture<PlayerSession> ensureSession(Player player) {
        return CompletableFuture.completedFuture(null);
    }
}
