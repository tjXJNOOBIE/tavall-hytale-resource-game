package com.tavall.resourcegame.events.middleware;

import com.tavall.resourcegame.events.core.EventSource;
import com.tavall.resourcegame.events.core.GameEvent;
import com.tavall.resourcegame.events.core.GameEventType;

import java.util.Map;
import java.util.UUID;

public record RedisGameEventEnvelope(
        UUID eventId,
        GameEventType eventType,
        UUID actorId,
        long createdAtEpochMillis,
        EventSource source,
        Map<String, String> attributes
) {
    public RedisGameEventEnvelope {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static RedisGameEventEnvelope from(GameEvent event) {
        return new RedisGameEventEnvelope(
                event.getEventId(),
                event.getEventType(),
                event.getActorId(),
                event.getCreatedAtEpochMillis(),
                event.getSource(),
                event.getAttributes()
        );
    }
}
