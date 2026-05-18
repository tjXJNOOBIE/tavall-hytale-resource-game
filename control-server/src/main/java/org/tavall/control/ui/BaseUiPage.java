package org.tavall.control.ui;

import org.tavall.api.minecraft.ui.UiActions;

import au.ellie.hyui.builders.HyUIPage;
import au.ellie.hyui.events.DynamicPageData;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.events.UIEventActions;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.tavall.control.ui.IUiActionHandler;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Base class for resource game UI pages.
 */
public abstract class BaseUiPage extends HyUIPage {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final Player player;
    private final UiNavigationContext context;
    private final PlayerGameState state;
    private final IUiActionHandler actionHandler;

    protected BaseUiPage(
            Player player,
            UiNavigationContext context,
            PlayerGameState state,
            IUiActionHandler actionHandler,
            String resourcePath,
            Map<String, ?> templateVariables,
            Collection<UiActionBinding> actionBindings
    ) {
        this(
                player,
                context,
                state,
                actionHandler,
                createDefinition(player, context, actionHandler, resourcePath, templateVariables, actionBindings)
        );
    }

    private BaseUiPage(
            Player player,
            UiNavigationContext context,
            PlayerGameState state,
            IUiActionHandler actionHandler,
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
        this.actionHandler = Objects.requireNonNull(actionHandler, "actionHandler");
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

    protected IUiActionHandler actionHandler() {
        return actionHandler;
    }

    @Override
    public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, DynamicPageData data) {
        super.handleDataEvent(ref, store, data);
        UiActionEventData directAction = directAction(data);
        if (directAction != null) {
            dispatch(player, context, actionHandler, directAction, this);
        }
    }

    private static UiPageDefinition createDefinition(
            Player player,
            UiNavigationContext context,
            IUiActionHandler actionHandler,
            String resourcePath,
            Map<String, ?> templateVariables,
            Collection<UiActionBinding> actionBindings
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(actionHandler, "actionHandler");
        return ResourceGameUiPageBuilder.build(
                resourcePath,
                templateVariables,
                actionBindings,
                (eventData, uiContext) -> dispatch(player, context, actionHandler, eventData, uiContext)
        );
    }

    private static void dispatch(
            Player player,
            UiNavigationContext context,
            IUiActionHandler actionHandler,
            UiActionEventData eventData,
            UIContext uiContext
    ) {
        if (eventData == null || eventData.action() == null || eventData.action().isBlank()) {
            return;
        }
        String action = eventData.action();
        LOGGER.at(Level.INFO).log(
                "Handling UI action %s from %s on %s.",
                action,
                player.getDisplayName(),
                "UI"
        );
        if (UiActions.CLOSE.equals(action)) {
            actionHandler.handleClose(player, context);
            if (uiContext != null) {
                uiContext.getPage().ifPresent(HyUIPage::close);
            }
            return;
        }
        actionHandler.handle(player, context, eventData);
    }

    static UiActionEventData directAction(DynamicPageData data) {
        if (data == null || data.action == null || data.action.isBlank()) {
            return null;
        }
        if (UIEventActions.BUTTON_CLICKED.equals(data.action)) {
            return null;
        }
        String payload = data.getValue(UiActionEventData.KEY_PAYLOAD);
        if (payload == null || payload.isBlank()) {
            return UiActionEventData.action(data.action);
        }
        return UiActionEventData.actionWithPayload(data.action, payload);
    }
}

