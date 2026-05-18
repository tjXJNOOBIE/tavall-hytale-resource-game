package org.tavall.control.ui;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;

/**
 * Factory for UI pages.
 */
public interface UiPageFactory {
    BaseUiPage create(Player player, UiNavigationContext context, PlayerGameState state);
}
