package com.tavall.resourcegame.ui;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tavall.resourcegame.dependency.interfaces.IInfrastructureHealthService;
import com.tavall.resourcegame.dependency.interfaces.IPlayerGameStateService;
import com.tavall.resourcegame.dependency.interfaces.IUiActionService;
import com.tavall.resourcegame.domain.PlayerGameState;
import com.tavall.resourcegame.domain.UiNavigationContext;

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
