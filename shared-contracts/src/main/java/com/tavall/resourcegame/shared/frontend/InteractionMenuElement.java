package com.tavall.resourcegame.shared.frontend;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record InteractionMenuElement(
        String elementId,
        int slot,
        String material,
        String displayName,
        List<String> lore,
        boolean enabled,
        String disabledReason,
        String actionId,
        Map<String, String> metadata
) {
    public InteractionMenuElement {
        Objects.requireNonNull(elementId, "elementId");
        Objects.requireNonNull(material, "material");
        Objects.requireNonNull(displayName, "displayName");
        if (elementId.isBlank()) {
            throw new IllegalArgumentException("elementId must not be blank");
        }
        if (material.isBlank()) {
            throw new IllegalArgumentException("material must not be blank");
        }
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        lore = lore == null ? List.of() : List.copyOf(lore);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
