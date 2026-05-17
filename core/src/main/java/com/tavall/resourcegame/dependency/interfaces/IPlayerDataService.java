package com.tavall.resourcegame.dependency.interfaces;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.services.PlayerSession;

import java.util.concurrent.CompletableFuture;

public interface IPlayerDataService extends IDependencyInjectableInterface {
    void handlePlayerReady(PlayerReadyEvent event);

    void handlePlayerDisconnect(PlayerDisconnectEvent event);

    CompletableFuture<PlayerSession> ensureSession(Player player);
}