package org.tavall.control.events.middleware;

import org.tavall.control.events.core.EventSource;
import org.tavall.control.events.core.GameEvent;
import org.tavall.control.events.core.GameEventType;

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
