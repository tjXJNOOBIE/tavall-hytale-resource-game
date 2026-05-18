package org.tavall.control.ui;

import org.tavall.api.minecraft.ui.UiActions;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;

import java.util.List;
import java.util.Map;

/**
 * Troops placeholder page.
 */
public final class CastleTroopsPage extends BaseUiPage {
    private static final String PAGE_DOCUMENT = "Pages/castle-troops.html";

    public CastleTroopsPage(Player player, UiNavigationContext context, PlayerGameState state) {
        super(player, context, state, PAGE_DOCUMENT, templateData(context, state), bindings());
    }

    private static Map<String, ?> templateData(UiNavigationContext context, PlayerGameState state) {
        return Map.ofEntries(
                Map.entry("TroopCount", String.valueOf(state.populationSummary().troopCount())),
                Map.entry("MightCount", String.valueOf(state.populationSummary().might())),
                Map.entry("FeedbackStatus", context.feedbackMessage().isBlank() ? "Right-click the soldier or troop anchor to inspect military status." : context.feedbackMessage())
        );
    }

    private static List<UiActionBinding> bindings() {
        return List.of(UiActionBinding.action("#BackButton", UiActions.OPEN_CASTLE_MAIN));
    }
}

