package org.tavall.minecraft.domain.ui;

import java.util.List;

public record UiSection(String title, List<String> lines, List<UiAction> actions, boolean placeholder) {
}
