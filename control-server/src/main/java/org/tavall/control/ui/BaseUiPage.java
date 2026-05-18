package org.tavall.control.ui;

import org.tavall.api.minecraft.ui.UiActions;

import au.ellie.hyui.builders.HyUIPage;
import au.ellie.hyui.events.UIContext;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Base class for resource game UI pages.
 */
public abstract class BaseUiPage extends HyUIPage {
    private final Player player;
    private final UiNavigationContext context;
    private final PlayerGameState state;

    protected BaseUiPage(
            Player player,
            UiNavigationContext context,
            PlayerGameState state,
            String resourcePath,
            Map<String, ?> templateVariables,
            Collection<UiActionBinding> actionBindings
    ) {
        this(
                player,
                context,
                state,
                createDefinition(resourcePath, templateVariables, actionBindings)
        );
    }

    private BaseUiPage(
            Player player,
            UiNavigationContext context,
            PlayerGameState state,
            UiPageDefinition definition
    ) {
        super(
                player.getPlayerRef(),
                CustomPageLifetime.CanDismiss,
                definition.uiFile(),
                definition.topLevelElements(),
                definition.editCallbacks(),
                definition.templateHtml(),
                definition.templateProcessor(),
                definition.runtimeTemplateUpdatesEnabled(),
                null,
                definition.rootElementBuilder()
        );
        this.player = Objects.requireNonNull(player, "player");
        this.context = Objects.requireNonNull(context, "context");
        this.state = Objects.requireNonNull(state, "state");
    }

    protected Player player() {
        return player;
    }

    protected UiNavigationContext context() {
        return context;
    }

    protected PlayerGameState state() {
        return state;
    }

    private static UiPageDefinition createDefinition(
            String resourcePath,
            Map<String, ?> templateVariables,
            Collection<UiActionBinding> actionBindings
    ) {
        return ResourceGameUiPageBuilder.build(
                resourcePath,
                templateVariables,
                actionBindings,
                BaseUiPage::handlePageAction
        );
    }

    private static void handlePageAction(UiActionEventData eventData, UIContext uiContext) {
        if (eventData == null || eventData.action() == null || !UiActions.CLOSE.equals(eventData.action()) || uiContext == null) {
            return;
        }
        uiContext.getPage().ifPresent(HyUIPage::close);
    }
}

