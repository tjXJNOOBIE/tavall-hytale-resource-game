package org.tavall.control.ui;
import org.tavall.control.player.PlayerGameStateHandler;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.control.runtime.IInfrastructureHealthHandler;
import org.tavall.control.player.IPlayerGameStateHandler;
import org.tavall.control.ui.IUiActionHandler;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;

import java.util.List;

/**
 * Debug command category page.
 */
public final class DebugCommandPage extends BaseUiPage {
    public DebugCommandPage(
            Player player,
            UiNavigationContext context,
            PlayerGameState state,
            IUiActionHandler actionHandler,
            IInfrastructureHealthHandler infrastructureHealthHandler,
            IPlayerGameStateHandler gameStateHandler,
            String pageDocument,
            List<UiActionBinding> bindings
    ) {
        super(
                player,
                context,
                state,
                actionHandler,
                pageDocument,
                DebugNavigatorPage.templateData(context, state, infrastructureHealthHandler, gameStateHandler),
                bindings
        );
    }
}

