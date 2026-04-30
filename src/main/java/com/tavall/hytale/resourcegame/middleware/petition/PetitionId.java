package com.tavall.hytale.resourcegame.middleware.petition;

import java.util.Objects;
import java.util.UUID;

public record PetitionId(UUID value) {
    public PetitionId {
        Objects.requireNonNull(value, "value");
    }

    public static PetitionId random() {
        return new PetitionId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
