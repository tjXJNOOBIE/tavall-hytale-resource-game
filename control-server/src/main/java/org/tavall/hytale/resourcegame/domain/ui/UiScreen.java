package org.tavall.minecraft.domain.ui;

import java.util.List;
import org.tavall.minecraft.runtime.HytaleAssetId;

public record UiScreen(String screenId, String title, HytaleAssetId frameAsset, List<UiSection> sections) {
}
