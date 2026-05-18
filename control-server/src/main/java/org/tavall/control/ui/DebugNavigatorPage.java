package org.tavall.control.ui;
import org.tavall.control.player.PlayerGameStateHandler;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.control.runtime.IInfrastructureHealthHandler;
import org.tavall.control.player.IPlayerGameStateHandler;
import org.tavall.control.ui.IUiActionHandler;
import org.tavall.control.domain.InfrastructureHealthSnapshot;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;

import java.util.List;
import java.util.Map;

/**
 * Debug navigator for UI pages.
 */
public final class DebugNavigatorPage extends BaseUiPage {
    private static final String PAGE_DOCUMENT = "Pages/debug-navigator.html";

    public DebugNavigatorPage(
            Player player,
            UiNavigationContext context,
            PlayerGameState state,
            IUiActionHandler actionHandler,
            IInfrastructureHealthHandler infrastructureHealthHandler,
            IPlayerGameStateHandler gameStateHandler
    ) {
        super(player, context, state, actionHandler, PAGE_DOCUMENT, templateData(context, state, infrastructureHealthHandler, gameStateHandler), bindings());
    }

    static Map<String, ?> templateData(
            UiNavigationContext context,
            PlayerGameState state,
            IInfrastructureHealthHandler infrastructureHealthHandler,
            IPlayerGameStateHandler gameStateHandler
    ) {
        InfrastructureHealthSnapshot healthSnapshot = infrastructureHealthHandler.snapshot();
        return Map.ofEntries(
                Map.entry("CacheStatus", healthSnapshot.cacheSummary()),
                Map.entry("PersistenceStatus", healthSnapshot.persistenceSummary()),
                Map.entry("InteriorTutorialStatus", tutorialStatus(gameStateHandler.isInteriorTutorialPending(state))),
                Map.entry("InteriorTourStatus", tutorialStatus(gameStateHandler.isInteriorTourPending(state))),
                Map.entry("UpgradeTutorialStatus", tutorialStatus(gameStateHandler.isUpgradeTutorialPending(state))),
                Map.entry(
                        "CommandFeedback",
                        context.feedbackMessage().isBlank()
                        ? "Use debug actions from here. Buttons route directly into Resource Game services."
                        : context.feedbackMessage()
                )
        );
    }

    static List<UiActionBinding> bindings() {
        return DebugUiCommandBindings.navigator();
    }

    private static String tutorialStatus(boolean pending) {
        return pending ? "pending" : "complete";
    }
}

