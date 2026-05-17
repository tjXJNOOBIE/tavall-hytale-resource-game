package com.tavall.resourcegame.api.internal.interaction;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record InteractionMenuModel(
        String menuId,
        String title,
        int size,
        InteractionTargetType targetType,
        String targetId,
        List<InteractionMenuElement> elements,
        Map<String, String> metadata
) {
    public InteractionMenuModel {
        Objects.requireNonNull(menuId, "menuId");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(targetType, "targetType");
        Objects.requireNonNull(targetId, "targetId");
        if (menuId.isBlank()) {
            throw new IllegalArgumentException("menuId must not be blank");
        }
        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (size <= 0 || size % 9 != 0) {
            throw new IllegalArgumentException("size must be a positive multiple of 9");
        }
        if (targetId.isBlank()) {
            throw new IllegalArgumentException("targetId must not be blank");
        }
        elements = elements == null ? List.of() : List.copyOf(elements);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
