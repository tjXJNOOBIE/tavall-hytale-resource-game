package com.tavall.resourcegame.ui;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tavall.resourcegame.domain.PlayerGameState;
import com.tavall.resourcegame.domain.UiNavigationContext;

/**
 * Factory for UI pages.
 */
public interface UiPageFactory {
    BaseUiPage create(Player player, UiNavigationContext context, PlayerGameState state);
}
