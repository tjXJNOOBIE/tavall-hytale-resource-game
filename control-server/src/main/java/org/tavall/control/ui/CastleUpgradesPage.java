package org.tavall.control.ui;

import org.tavall.api.minecraft.ui.UiActions;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.control.ui.IUiActionHandler;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;

import java.util.List;
import java.util.Map;

/**
 * Citizen-to-troop upgrade page.
 */
public final class CastleUpgradesPage extends BaseUiPage {
    private static final String PAGE_DOCUMENT = "Pages/castle-upgrades.html";

    public CastleUpgradesPage(Player player, UiNavigationContext context, PlayerGameState state, IUiActionHandler actionHandler) {
        super(player, context, state, actionHandler, PAGE_DOCUMENT, templateData(context, state, actionHandler), bindings());
    }

    private static Map<String, ?> templateData(UiNavigationContext context, PlayerGameState state, IUiActionHandler actionHandler) {
        UpgradeActionState promoteState = actionHandler.promoteActionState(state);
        UpgradeActionState demoteState = actionHandler.demoteActionState(state);
        return Map.ofEntries(
                Map.entry("CitizenCount", String.valueOf(state.populationSummary().citizenCount())),
                Map.entry("TroopCount", String.valueOf(state.populationSummary().troopCount())),
                Map.entry("FoodCount", String.valueOf(state.resources().food())),
                Map.entry("WoodCount", String.valueOf(state.resources().wood())),
                Map.entry("IronCount", String.valueOf(state.resources().iron())),
                Map.entry("PromotionCost", actionHandler.promotionCostSummary(state)),
                Map.entry("PromoteStatus", promoteState.message()),
                Map.entry("DemoteStatus", demoteState.message()),
                Map.entry("TutorialStatus", actionHandler.upgradeTutorialMessage(state)),
                Map.entry("FeedbackStatus", context.feedbackMessage().isBlank() ? "Awaiting action." : context.feedbackMessage())
        );
    }

    private static List<UiActionBinding> bindings() {
        return List.of(
                UiActionBinding.action("#PromoteButton", UiActions.PROMOTE),
                UiActionBinding.action("#DemoteButton", UiActions.DEMOTE),
                UiActionBinding.action("#BackButton", UiActions.OPEN_CASTLE_MAIN)
        );
    }
}

