package org.tavall.control.liveops.gui;

import java.util.Map;

public record GlobalGuiChangeRequest(
        String guiKey,
        String title,
        String layoutJson,
        boolean enabled,
        String updatedBy,
        Map<String, String> metadata
) {
    public GlobalGuiChangeRequest {
        if (guiKey == null || guiKey.isBlank()) {
            throw new IllegalArgumentException("guiKey is required.");
        }
        title = title == null ? "" : title;
        layoutJson = layoutJson == null || layoutJson.isBlank() ? "{}" : layoutJson;
        updatedBy = updatedBy == null || updatedBy.isBlank() ? "system" : updatedBy;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
