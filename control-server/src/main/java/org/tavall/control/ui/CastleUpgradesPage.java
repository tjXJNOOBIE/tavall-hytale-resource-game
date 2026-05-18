package org.tavall.control.ui;

import org.tavall.api.minecraft.ui.UiActions;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.control.player.IPlayerGameStateHandler;
import org.tavall.control.population.IPopulationHandler;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;

import java.util.List;
import java.util.Map;

/**
 * Citizen-to-troop upgrade page.
 */
public final class CastleUpgradesPage extends BaseUiPage {
    private static final String PAGE_DOCUMENT = "Pages/castle-upgrades.html";

    public CastleUpgradesPage(
            Player player,
            UiNavigationContext context,
            PlayerGameState state,
            IPopulationHandler populationHandler,
            IPlayerGameStateHandler gameStateHandler
    ) {
        super(player, context, state, PAGE_DOCUMENT, templateData(context, state, populationHandler, gameStateHandler), bindings());
    }

    private static Map<String, ?> templateData(
            UiNavigationContext context,
            PlayerGameState state,
            IPopulationHandler populationHandler,
            IPlayerGameStateHandler gameStateHandler
    ) {
        UpgradeActionState promoteState = populationHandler.promoteActionState(state);
        UpgradeActionState demoteState = populationHandler.demoteActionState(state);
        return Map.ofEntries(
                Map.entry("CitizenCount", String.valueOf(state.populationSummary().citizenCount())),
                Map.entry("TroopCount", String.valueOf(state.populationSummary().troopCount())),
                Map.entry("FoodCount", String.valueOf(state.resources().food())),
                Map.entry("WoodCount", String.valueOf(state.resources().wood())),
                Map.entry("IronCount", String.valueOf(state.resources().iron())),
                Map.entry("PromotionCost", populationHandler.promotionCostSummary(state)),
                Map.entry("PromoteStatus", promoteState.message()),
                Map.entry("DemoteStatus", demoteState.message()),
                Map.entry("TutorialStatus", upgradeTutorialMessage(state, gameStateHandler)),
                Map.entry("FeedbackStatus", context.feedbackMessage().isBlank() ? "Awaiting action." : context.feedbackMessage())
        );
    }

    private static String upgradeTutorialMessage(PlayerGameState state, IPlayerGameStateHandler gameStateHandler) {
        if (gameStateHandler.isUpgradeTutorialPending(state)) {
            return "Step 1: confirm citizens and troops. Step 2: check the Food, Wood, and Iron cost. Step 3: promote once the route is ready.";
        }
        return "Tutorial complete: use this page to convert citizens when resources allow.";
    }

    private static List<UiActionBinding> bindings() {
        return List.of(
                UiActionBinding.action("#PromoteButton", UiActions.PROMOTE),
                UiActionBinding.action("#DemoteButton", UiActions.DEMOTE),
                UiActionBinding.action("#BackButton", UiActions.OPEN_CASTLE_MAIN)
        );
    }
}

