package com.tavall.hytale.resourcegame.middleware.event;

import java.time.Instant;
import java.util.Map;

public interface DomainEvent {
    String eventType();

    Instant occurredAt();

    Map<String, String> attributes();
}
