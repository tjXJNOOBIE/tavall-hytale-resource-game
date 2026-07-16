package org.tavall.minecraft.server;

import org.tavall.api.minecraft.interaction.InteractionTargetType;

import java.util.Map;
import java.util.Objects;

record MinecraftBukkitInteractionTarget(
        InteractionTargetType targetType,
        String targetId,
        String displayName,
        Map<String, String> metadata
) {
    MinecraftBukkitInteractionTarget {
        Objects.requireNonNull(targetType, "targetType");
        Objects.requireNonNull(targetId, "targetId");
        if (targetId.isBlank()) {
            throw new IllegalArgumentException("targetId must not be blank");
        }
        displayName = displayName == null || displayName.isBlank() ? targetType.name() : displayName;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
