package org.tavall.control.dependency.interfaces;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.control.services.PlayerSession;

import java.util.Collection;
import java.util.UUID;

public interface IPlayerSessionStore extends IDependencyInjectableInterface {
    PlayerSession get(UUID playerId);

    void put(PlayerSession session);

    void remove(UUID playerId);

    Collection<PlayerSession> snapshot();
}
