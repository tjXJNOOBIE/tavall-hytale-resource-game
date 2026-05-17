package com.tavall.resourcegame.dependency.interfaces;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.services.PlayerSession;

import java.util.Collection;
import java.util.UUID;

public interface IPlayerSessionStore extends IDependencyInjectableInterface {
    PlayerSession get(UUID playerId);

    void put(PlayerSession session);

    void remove(UUID playerId);

    Collection<PlayerSession> snapshot();
}
