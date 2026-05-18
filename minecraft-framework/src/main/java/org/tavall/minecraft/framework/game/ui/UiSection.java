package org.tavall.minecraft.framework.game.ui;

import java.util.List;

public record UiSection(String title, List<String> lines, List<UiAction> actions, boolean placeholder) {
    public UiSection {
        lines = lines == null ? List.of() : List.copyOf(lines);
        actions = actions == null ? List.of() : List.copyOf(actions);
    }
}
