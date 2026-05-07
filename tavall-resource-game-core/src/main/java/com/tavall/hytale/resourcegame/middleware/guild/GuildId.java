package com.tavall.hytale.resourcegame.middleware.guild;

import java.util.Objects;
import java.util.UUID;

public record GuildId(UUID value) {
    public GuildId {
        Objects.requireNonNull(value, "value");
    }

    public static GuildId random() {
        return new GuildId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
