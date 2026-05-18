package org.tavall.control.ui;

import org.tavall.api.minecraft.ui.UiActions;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.control.ui.IUiActionHandler;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;

import java.util.List;
import java.util.Map;

/**
 * Castle info placeholder page.
 */
public final class CastleInfoPage extends BaseUiPage {
    private static final String PAGE_DOCUMENT = "Pages/castle-info.html";

    public CastleInfoPage(Player player, UiNavigationContext context, PlayerGameState state, IUiActionHandler actionHandler) {
        super(player, context, state, actionHandler, PAGE_DOCUMENT, templateData(context, state), bindings());
    }

    private static Map<String, ?> templateData(UiNavigationContext context, PlayerGameState state) {
        return Map.ofEntries(
                Map.entry("CastleId", state.castleId() == null ? "Unassigned" : state.castleId().toString()),
                Map.entry("WorldName", state.castleLocation() == null ? "Unknown" : state.castleLocation().worldName()),
                Map.entry("OwnerName", context.playerName()),
                Map.entry("TroopCount", String.valueOf(state.populationSummary().troopCount())),
                Map.entry("MightCount", String.valueOf(state.populationSummary().might())),
                Map.entry("FeedbackStatus", context.feedbackMessage().isBlank() ? "Right-click the focused castle in-world to reopen this command surface." : context.feedbackMessage())
        );
    }

    private static List<UiActionBinding> bindings() {
        return List.of(
                UiActionBinding.action("#AttackButton", UiActions.CASTLE_ATTACK_PLACEHOLDER),
                UiActionBinding.action("#FriendButton", UiActions.CASTLE_FRIEND_PLACEHOLDER),
                UiActionBinding.action("#GuildButton", UiActions.CASTLE_GUILD_PLACEHOLDER),
                UiActionBinding.action("#BackButton", UiActions.OPEN_CASTLE_MAIN)
        );
    }
}

