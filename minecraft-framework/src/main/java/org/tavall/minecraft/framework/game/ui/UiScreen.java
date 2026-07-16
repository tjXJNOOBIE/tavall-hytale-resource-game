package org.tavall.minecraft.framework.game.ui;

import org.tavall.minecraft.framework.game.AssetId;

import java.util.List;

public record UiScreen(String screenId, String title, AssetId frameAsset, List<UiSection> sections) {
    public UiScreen {
        sections = sections == null ? List.of() : List.copyOf(sections);
    }
}
