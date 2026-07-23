package org.tavall.control.player;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import org.tavall.dependency.IDependencyInjectableInterface;
import org.tavall.control.player.PlayerSession;

import java.util.concurrent.CompletableFuture;

public interface IPlayerDataHandler extends IDependencyInjectableInterface {
    void handlePlayerReady(PlayerReadyEvent event);

    void handlePlayerDisconnect(PlayerDisconnectEvent event);

    CompletableFuture<PlayerSession> ensureSession(Player player);
}
