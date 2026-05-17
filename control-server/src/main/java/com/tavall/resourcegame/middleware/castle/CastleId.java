package com.tavall.resourcegame.middleware.castle;

import java.util.Objects;
import java.util.UUID;

public record CastleId(UUID value) {
    public CastleId {
        Objects.requireNonNull(value, "value");
    }

    public static CastleId random() {
        return new CastleId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
