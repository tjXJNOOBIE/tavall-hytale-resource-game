package org.tavall.control.ui;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.control.dependency.interfaces.IInfrastructureHealthService;
import org.tavall.control.dependency.interfaces.IPlayerGameStateService;
import org.tavall.control.dependency.interfaces.IUiActionService;
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
            IUiActionService actionService,
            IInfrastructureHealthService infrastructureHealthService,
            IPlayerGameStateService gameStateService
    ) {
        super(player, context, state, actionService, PAGE_DOCUMENT, templateData(context, state, infrastructureHealthService, gameStateService), bindings());
    }

    static Map<String, ?> templateData(
            UiNavigationContext context,
            PlayerGameState state,
            IInfrastructureHealthService infrastructureHealthService,
            IPlayerGameStateService gameStateService
    ) {
        InfrastructureHealthSnapshot healthSnapshot = infrastructureHealthService.snapshot();
        return Map.ofEntries(
                Map.entry("CacheStatus", healthSnapshot.cacheSummary()),
                Map.entry("PersistenceStatus", healthSnapshot.persistenceSummary()),
                Map.entry("InteriorTutorialStatus", tutorialStatus(gameStateService.isInteriorTutorialPending(state))),
                Map.entry("InteriorTourStatus", tutorialStatus(gameStateService.isInteriorTourPending(state))),
                Map.entry("UpgradeTutorialStatus", tutorialStatus(gameStateService.isUpgradeTutorialPending(state))),
                Map.entry(
                        "CommandFeedback",
                        context.feedbackMessage().isBlank()
                        ? "Use debug actions from here. Buttons route directly into Resource Game services."
                        : context.feedbackMessage()
                )
        );
    }

    static List<HyUiActionBinding> bindings() {
        return DebugUiCommandBindings.navigator();
    }

    private static String tutorialStatus(boolean pending) {
        return pending ? "pending" : "complete";
    }
}
