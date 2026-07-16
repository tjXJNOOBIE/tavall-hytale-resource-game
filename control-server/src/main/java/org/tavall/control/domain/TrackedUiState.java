package org.tavall.control.domain;

import org.tavall.minecraft.framework.game.ui.UiScreenKey;

import java.util.Objects;

/**
 * Stores the last tracked UI page and navigation context for a player.
 */
public final class TrackedUiState {
    private final UiScreenKey screenKey;
    private final UiNavigationContext navigationContext;
    private final String stateFingerprint;

    public TrackedUiState(UiScreenKey screenKey, UiNavigationContext navigationContext, String stateFingerprint) {
        this.screenKey = Objects.requireNonNull(screenKey, "screenKey");
        this.navigationContext = Objects.requireNonNull(navigationContext, "navigationContext");
        this.stateFingerprint = stateFingerprint == null ? "" : stateFingerprint;
    }

    public UiScreenKey screenKey() {
        return screenKey;
    }

    public UiNavigationContext navigationContext() {
        return navigationContext;
    }

    public String stateFingerprint() {
        return stateFingerprint;
    }
}
