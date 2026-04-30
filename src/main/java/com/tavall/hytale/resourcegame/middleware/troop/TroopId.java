package com.tavall.hytale.resourcegame.middleware.troop;

import java.util.Objects;
import java.util.UUID;

public record TroopId(UUID value) {
    public TroopId {
        Objects.requireNonNull(value, "value");
    }

    public static TroopId random() {
        return new TroopId(UUID.randomUUID());
    }
}
