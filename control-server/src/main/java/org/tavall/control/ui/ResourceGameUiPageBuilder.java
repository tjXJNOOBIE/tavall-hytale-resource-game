package org.tavall.control.ui;

import au.ellie.hyui.builders.PageBuilder;
import au.ellie.hyui.events.UIContext;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Creates UI page definitions while preserving Resource Game page class names.
 */
public final class ResourceGameUiPageBuilder extends PageBuilder {
    public static UiPageDefinition build(
            String resourcePath,
            Map<String, ?> templateVariables,
            Collection<UiActionBinding> actionBindings,
            BiConsumer<UiActionEventData, UIContext> actionHandler
    ) {
        ResourceGameUiPageBuilder builder = new ResourceGameUiPageBuilder();
        String templateHtml = UiPageMarkupDecorator.loadDecorated(Objects.requireNonNull(resourcePath, "resourcePath"));
        builder.fromTemplate(templateHtml, templateVariables == null ? Map.of() : templateVariables);
        for (UiActionBinding binding : actionBindings == null ? List.<UiActionBinding>of() : actionBindings) {
            builder.addEventListener(
                    binding.elementId(),
                    CustomUIEventBindingType.Activating,
                    Void.class,
                    (ignored, uiContext) -> actionHandler.accept(binding.eventData(), uiContext)
            );
        }
        return builder.definition();
    }

    private UiPageDefinition definition() {
        return new UiPageDefinition(
                uiFile,
                List.copyOf(getTopLevelElements()),
                List.copyOf(editCallbacks),
                templateHtml,
                templateProcessor,
                runtimeTemplateUpdatesEnabled,
                this
        );
    }
}
