package com.tavall.hytale.resourcegame.middleware.event;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record SimpleDomainEvent(
        String eventType,
        Instant occurredAt,
        Map<String, String> attributes
) implements DomainEvent {
    public SimpleDomainEvent {
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType is required.");
        }
        Objects.requireNonNull(occurredAt, "occurredAt");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
