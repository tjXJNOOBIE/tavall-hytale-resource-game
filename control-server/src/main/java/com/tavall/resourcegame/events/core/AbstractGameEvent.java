package org.tavall.control.events.core;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public abstract class AbstractGameEvent implements GameEvent {
    public static final UUID SYSTEM_ACTOR_ID = new UUID(0L, 0L);

    private final UUID eventId;
    private final GameEventType eventType;
    private final UUID actorId;
    private final long createdAtEpochMillis;
    private final EventSource source;
    private final Map<String, String> attributes;

    protected AbstractGameEvent(
            UUID eventId,
            GameEventType eventType,
            UUID actorId,
            long createdAtEpochMillis,
            EventSource source,
            Map<String, String> attributes
    ) {
        this.eventId = eventId == null ? UUID.randomUUID() : eventId;
        this.eventType = Objects.requireNonNull(eventType, "eventType");
        this.actorId = actorId == null ? SYSTEM_ACTOR_ID : actorId;
        this.createdAtEpochMillis = createdAtEpochMillis <= 0L ? System.currentTimeMillis() : createdAtEpochMillis;
        this.source = Objects.requireNonNull(source, "source");
        this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public GameEventType getEventType() {
        return eventType;
    }

    @Override
    public UUID getActorId() {
        return actorId;
    }

    @Override
    public long getCreatedAtEpochMillis() {
        return createdAtEpochMillis;
    }

    @Override
    public EventSource getSource() {
        return source;
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }
}
