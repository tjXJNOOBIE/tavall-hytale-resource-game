package org.tavall.control.ui;

import org.tavall.api.minecraft.ui.UiActions;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.control.player.IPlayerGameStateHandler;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;

import java.util.List;
import java.util.Map;

/**
 * Interior overview page.
 */
public final class InteriorMainPage extends BaseUiPage {
    private static final String PAGE_DOCUMENT = "Pages/interior-main.html";

    public InteriorMainPage(Player player, UiNavigationContext context, PlayerGameState state, IPlayerGameStateHandler gameStateHandler) {
        super(player, context, state, PAGE_DOCUMENT, templateData(context, state, gameStateHandler), bindings());
    }

    private static Map<String, ?> templateData(UiNavigationContext context, PlayerGameState state, IPlayerGameStateHandler gameStateHandler) {
        return Map.of(
                "TutorialStatus",
                context.feedbackMessage().isBlank()
                        ? interiorTutorialMessage(state, gameStateHandler)
                        : context.feedbackMessage()
        );
    }

    private static String interiorTutorialMessage(PlayerGameState state, IPlayerGameStateHandler gameStateHandler) {
        if (gameStateHandler.isInteriorTutorialPending(state) || gameStateHandler.isInteriorTourPending(state)) {
            return "Step 1: follow the tour markers. Step 2: inspect the citizen and troop anchors. Step 3: leave through the exit lane when you are done.";
        }
        return "Interior tutorial complete: citizen and troop anchors stay here while the upgrade pipeline grows.";
    }

    private static List<UiActionBinding> bindings() {
        return List.of(
                UiActionBinding.action("#ExitInteriorButton", UiActions.EXIT_INTERIOR),
                UiActionBinding.action("#BuildingsButton", UiActions.OPEN_BUILDINGS),
                UiActionBinding.action("#BackButton", UiActions.OPEN_CASTLE_MAIN)
        );
    }
}

