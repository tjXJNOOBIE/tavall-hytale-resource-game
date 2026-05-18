package org.tavall.control.ui;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;
import org.tavall.control.ui.UiActionEventData;
import org.tavall.control.ui.UpgradeActionState;

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

