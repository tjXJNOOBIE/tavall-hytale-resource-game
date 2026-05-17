package com.tavall.resourcegame.dependency.interfaces;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.domain.PlayerGameState;
import com.tavall.resourcegame.domain.UiNavigationContext;
import com.tavall.resourcegame.ui.UiActionEventData;
import com.tavall.resourcegame.ui.UpgradeActionState;

public interface IUiActionService extends IDependencyInjectableInterface {
    void handle(Player player, UiNavigationContext context, UiActionEventData eventData);

    default void handleClose(Player player, UiNavigationContext context) {
    }

    UpgradeActionState promoteActionState(PlayerGameState state);

    UpgradeActionState demoteActionState(PlayerGameState state);

    String promotionCostSummary(PlayerGameState state);

    String upgradeTutorialMessage(PlayerGameState state);

    String interiorTutorialMessage(PlayerGameState state);
}
