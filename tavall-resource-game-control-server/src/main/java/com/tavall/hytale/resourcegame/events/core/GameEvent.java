package com.tavall.hytale.resourcegame.events.core;

import java.util.Map;
import java.util.UUID;

public interface GameEvent {
    UUID getEventId();

    GameEventType getEventType();

    UUID getActorId();

    long getCreatedAtEpochMillis();

    EventSource getSource();

    Map<String, String> getAttributes();
}
