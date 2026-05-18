package org.tavall.control.ui;

import org.tavall.api.minecraft.ui.UiActions;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.control.ui.IUiActionHandler;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;

import java.util.List;
import java.util.Map;

/**
 * Interior overview page.
 */
public final class InteriorMainPage extends BaseUiPage {
    private static final String PAGE_DOCUMENT = "Pages/interior-main.html";

    public InteriorMainPage(Player player, UiNavigationContext context, PlayerGameState state, IUiActionHandler actionHandler) {
        super(player, context, state, actionHandler, PAGE_DOCUMENT, templateData(context, state, actionHandler), bindings());
    }

    private static Map<String, ?> templateData(UiNavigationContext context, PlayerGameState state, IUiActionHandler actionHandler) {
        return Map.of(
                "TutorialStatus",
                context.feedbackMessage().isBlank()
                        ? actionHandler.interiorTutorialMessage(state)
                        : context.feedbackMessage()
        );
    }

    private static List<UiActionBinding> bindings() {
        return List.of(
                UiActionBinding.action("#ExitInteriorButton", UiActions.EXIT_INTERIOR),
                UiActionBinding.action("#BuildingsButton", UiActions.OPEN_BUILDINGS),
                UiActionBinding.action("#BackButton", UiActions.OPEN_CASTLE_MAIN)
        );
    }
}

