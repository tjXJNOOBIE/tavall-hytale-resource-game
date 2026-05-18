package org.tavall.control.ui;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.control.dependency.interfaces.IInfrastructureHealthService;
import org.tavall.control.dependency.interfaces.IPlayerGameStateService;
import org.tavall.control.dependency.interfaces.IUiActionService;
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
            IUiActionService actionService,
            IInfrastructureHealthService infrastructureHealthService,
            IPlayerGameStateService gameStateService,
            String pageDocument,
            List<HyUiActionBinding> bindings
    ) {
        super(
                player,
                context,
                state,
                actionService,
                pageDocument,
                DebugNavigatorPage.templateData(context, state, infrastructureHealthService, gameStateService),
                bindings
        );
    }
}
