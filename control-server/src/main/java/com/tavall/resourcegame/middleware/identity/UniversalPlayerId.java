package com.tavall.resourcegame.middleware.identity;

import java.util.Objects;
import java.util.UUID;

public record UniversalPlayerId(UUID value) {
    public UniversalPlayerId {
        Objects.requireNonNull(value, "value");
    }

    public static UniversalPlayerId random() {
        return new UniversalPlayerId(UUID.randomUUID());
    }

    public static UniversalPlayerId of(UUID value) {
        return new UniversalPlayerId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
