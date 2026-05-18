package org.tavall.minecraft.domain.ui;

import java.util.List;
import org.tavall.minecraft.runtime.AssetId;

public record UiScreen(String screenId, String title, AssetId frameAsset, List<UiSection> sections) {
}
