package com.tavall.resourcegame.dependency.interfaces;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.domain.PlayerGameState;
import com.tavall.resourcegame.domain.UiNavigationContext;
import com.tavall.resourcegame.ui.UiPageType;

import java.util.UUID;

public interface IUiNavigator extends IDependencyInjectableInterface {
    void open(UiPageType type, Player player, UiNavigationContext context, PlayerGameState state);

    void refreshTrackedPage(UUID playerId, PlayerGameState state);

    void clearTrackedPage(UUID playerId);
}
