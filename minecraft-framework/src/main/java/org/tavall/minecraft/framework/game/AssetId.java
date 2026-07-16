package org.tavall.minecraft.framework.game;

import java.util.Objects;

public record AssetId(String value) {
    public AssetId {
        value = Objects.requireNonNull(value, "value").trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("value cannot be blank");
        }
    }
}
