package com.tavall.resourcegame.middleware.asset;

import java.util.Objects;

public record GlobalAssetId(String value) {
    public GlobalAssetId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("global asset id is required.");
        }
        value = value.toLowerCase();
        if (!value.matches("[a-z0-9][a-z0-9_.-]*")) {
            throw new IllegalArgumentException("global asset id contains unsupported characters.");
        }
    }

    public static GlobalAssetId of(String value) {
        return new GlobalAssetId(value);
    }

    @Override
    public String toString() {
        return Objects.toString(value);
    }
}
