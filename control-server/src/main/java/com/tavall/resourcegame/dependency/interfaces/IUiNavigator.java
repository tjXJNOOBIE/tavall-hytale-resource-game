package org.tavall.control.dependency.interfaces;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;
import org.tavall.control.ui.UiPageType;

import java.util.UUID;

public interface IUiNavigator extends IDependencyInjectableInterface {
    void open(UiPageType type, Player player, UiNavigationContext context, PlayerGameState state);

    void refreshTrackedPage(UUID playerId, PlayerGameState state);

    void clearTrackedPage(UUID playerId);
}
