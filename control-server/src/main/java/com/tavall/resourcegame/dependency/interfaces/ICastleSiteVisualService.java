package com.tavall.resourcegame.dependency.interfaces;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.domain.PlayerGameState;

import java.util.UUID;

public interface ICastleSiteVisualService extends IDependencyInjectableInterface {
    void ensureSite(UUID playerId, PlayerGameState state);

    void refreshSite(UUID playerId, PlayerGameState state);

    void clearSite(UUID playerId);
}
