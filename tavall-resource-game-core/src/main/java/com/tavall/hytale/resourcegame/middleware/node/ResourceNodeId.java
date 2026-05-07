package com.tavall.hytale.resourcegame.middleware.node;

import java.util.Objects;
import java.util.UUID;

public record ResourceNodeId(UUID value) {
    public ResourceNodeId {
        Objects.requireNonNull(value, "value");
    }

    public static ResourceNodeId random() {
        return new ResourceNodeId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
