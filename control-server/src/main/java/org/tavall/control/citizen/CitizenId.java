package org.tavall.control.citizen;

import java.util.Objects;
import java.util.UUID;

public record CitizenId(UUID value) {
    public CitizenId {
        Objects.requireNonNull(value, "value");
    }

    public static CitizenId random() {
        return new CitizenId(UUID.randomUUID());
    }

    public static CitizenId of(String value) {
        return new CitizenId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
