package org.tavall.control.ui;

import org.tavall.api.minecraft.ui.UiActions;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;
import org.tavall.control.castle.CastleEconomyPlanner;

import java.util.List;
import java.util.Map;

/**
 * Main castle UI page.
 */
public final class CastleMainPage extends BaseUiPage {
    private static final String PAGE_DOCUMENT = "Pages/castle-main.html";

    public CastleMainPage(
            Player player,
            UiNavigationContext context,
            PlayerGameState state,
            CastleEconomyPlanner economyPlanner
    ) {
        super(player, context, state, PAGE_DOCUMENT, templateData(state, economyPlanner), bindings());
    }

    private static Map<String, ?> templateData(PlayerGameState state, CastleEconomyPlanner economyPlanner) {
        return Map.ofEntries(
                Map.entry("CitizenCount", String.valueOf(state.populationSummary().citizenCount())),
                Map.entry("TroopCount", String.valueOf(state.populationSummary().troopCount())),
                Map.entry("MightCount", String.valueOf(state.populationSummary().might())),
                Map.entry("FoodCount", String.valueOf(state.resources().food())),
                Map.entry("WoodCount", String.valueOf(state.resources().wood())),
                Map.entry("IronCount", String.valueOf(state.resources().iron())),
                Map.entry("Subtitle", economyPlanner.workforceSummary(state))
        );
    }

    private static List<UiActionBinding> bindings() {
        return List.of(
                UiActionBinding.action("#EnterInteriorButton", UiActions.ENTER_INTERIOR),
                UiActionBinding.action("#CastleInfoButton", UiActions.OPEN_CASTLE_INFO),
                UiActionBinding.action("#CitizensButton", UiActions.OPEN_CITIZENS),
                UiActionBinding.action("#TroopsButton", UiActions.OPEN_TROOPS),
                UiActionBinding.action("#ResourcesButton", UiActions.OPEN_RESOURCES),
                UiActionBinding.action("#UpgradesButton", UiActions.OPEN_UPGRADES),
                UiActionBinding.action("#BuildingsButton", UiActions.OPEN_BUILDINGS),
                UiActionBinding.action("#AttackButton", UiActions.CASTLE_ATTACK_PLACEHOLDER),
                UiActionBinding.action("#FriendButton", UiActions.CASTLE_FRIEND_PLACEHOLDER),
                UiActionBinding.action("#GuildButton", UiActions.CASTLE_GUILD_PLACEHOLDER),
                UiActionBinding.action("#CloseButton", UiActions.CLOSE)
        );
    }
}

