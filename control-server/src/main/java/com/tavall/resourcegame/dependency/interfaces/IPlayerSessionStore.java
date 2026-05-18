package org.tavall.control.dependency.interfaces;
import org.tavall.control.player.PlayerSessionStore;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.control.player.PlayerSession;

import java.util.Collection;
import java.util.UUID;

public interface IPlayerSessionStore extends IDependencyInjectableInterface {
    PlayerSession get(UUID playerId);

    void put(PlayerSession session);

    void remove(UUID playerId);

    Collection<PlayerSession> snapshot();
}
