package com.tavall.resourcegame.middleware.control;

import java.util.Objects;
import java.util.UUID;

public record ControlCommandId(UUID value) {
    public ControlCommandId {
        Objects.requireNonNull(value, "value");
    }

    public static ControlCommandId random() {
        return new ControlCommandId(UUID.randomUUID());
    }

    public static ControlCommandId of(String value) {
        return new ControlCommandId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
