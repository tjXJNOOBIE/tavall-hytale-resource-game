package org.tavall.control.ui;

import org.tavall.api.minecraft.ui.UiActions;

import java.util.Objects;

/**
 * Maps one UI element activation to the existing UI action pipeline.
 */
public record UiActionBinding(String elementId, String action, String payload) {
    public static final String COMMAND_RETURN_SEPARATOR = "\u001F";

    public UiActionBinding {
        elementId = normalizeElementId(elementId);
        action = Objects.requireNonNull(action, "action");
        payload = payload == null ? "" : payload;
        if (elementId.isBlank()) {
            throw new IllegalArgumentException("elementId cannot be blank");
        }
        if (action.isBlank()) {
            throw new IllegalArgumentException("action cannot be blank");
        }
    }

    public static UiActionBinding action(String selectorOrElementId, String action) {
        return new UiActionBinding(selectorOrElementId, action, "");
    }

    public static UiActionBinding action(String selectorOrElementId, String action, String payload) {
        return new UiActionBinding(selectorOrElementId, action, payload);
    }

    public static UiActionBinding action(String selectorOrElementId, String action, String payload, UiPageType returnPage) {
        String returnPayload = returnPage == null || payload == null || payload.isBlank()
                ? payload
                : returnPage.name() + COMMAND_RETURN_SEPARATOR + payload;
        return new UiActionBinding(selectorOrElementId, action, returnPayload);
    }

    public static UiActionBinding command(String selectorOrElementId, String commandLine) {
        return new UiActionBinding(selectorOrElementId, UiActions.RUN_COMMAND, commandLine);
    }

    public static UiActionBinding command(String selectorOrElementId, String commandLine, UiPageType returnPage) {
        String returnPayload = returnPage == null
                ? commandLine
                : returnPage.name() + COMMAND_RETURN_SEPARATOR + commandLine;
        return new UiActionBinding(selectorOrElementId, UiActions.RUN_COMMAND, returnPayload);
    }

    public UiActionEventData eventData() {
        if (payload.isBlank()) {
            return UiActionEventData.action(action);
        }
        return UiActionEventData.actionWithPayload(action, payload);
    }

    private static String normalizeElementId(String selectorOrElementId) {
        String normalized = Objects.requireNonNull(selectorOrElementId, "selectorOrElementId").trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        int dotIndex = normalized.indexOf('.');
        if (dotIndex >= 0) {
            normalized = normalized.substring(0, dotIndex);
        }
        return normalized;
    }
}
