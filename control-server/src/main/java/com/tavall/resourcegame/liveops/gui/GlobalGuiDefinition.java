package com.tavall.resourcegame.liveops.gui;

import java.time.Instant;
import java.util.UUID;

public record GlobalGuiDefinition(
        UUID guiId,
        String guiKey,
        String title,
        String layoutJson,
        long version,
        boolean enabled,
        Instant updatedAt,
        String updatedBy
) {
    public GlobalGuiDefinition {
        guiId = guiId == null ? UUID.randomUUID() : guiId;
        if (guiKey == null || guiKey.isBlank()) {
            throw new IllegalArgumentException("guiKey is required.");
        }
        title = title == null ? "" : title;
        layoutJson = layoutJson == null || layoutJson.isBlank() ? "{}" : layoutJson;
        version = Math.max(1L, version);
        updatedAt = updatedAt == null ? Instant.now() : updatedAt;
        updatedBy = updatedBy == null || updatedBy.isBlank() ? "system" : updatedBy;
    }

    public GlobalGuiDefinition withVersionedLayout(String nextTitle, String nextLayoutJson, boolean nextEnabled, String nextUpdatedBy, Instant now) {
        return new GlobalGuiDefinition(guiId, guiKey, nextTitle, nextLayoutJson, version + 1, nextEnabled, now, nextUpdatedBy);
    }
}
