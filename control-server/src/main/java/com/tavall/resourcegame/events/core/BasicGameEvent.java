package com.tavall.resourcegame.events.core;

import java.util.Map;
import java.util.UUID;

public final class BasicGameEvent extends AbstractGameEvent {
    public BasicGameEvent(GameEventType eventType, UUID actorId, EventSource source, Map<String, String> attributes) {
        super(UUID.randomUUID(), eventType, actorId, System.currentTimeMillis(), source, attributes);
    }

    public BasicGameEvent(UUID eventId, GameEventType eventType, UUID actorId, long createdAtEpochMillis, EventSource source, Map<String, String> attributes) {
        super(eventId, eventType, actorId, createdAtEpochMillis, source, attributes);
    }
}
