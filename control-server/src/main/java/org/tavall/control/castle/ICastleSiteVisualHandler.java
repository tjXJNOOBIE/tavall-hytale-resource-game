package org.tavall.control.castle;

import org.tavall.dependency.IDependencyInjectableInterface;
import org.tavall.control.domain.PlayerGameState;

import java.util.UUID;

public interface ICastleSiteVisualHandler extends IDependencyInjectableInterface {
    void ensureSite(UUID playerId, PlayerGameState state);

    void refreshSite(UUID playerId, PlayerGameState state);

    void clearSite(UUID playerId);
}

