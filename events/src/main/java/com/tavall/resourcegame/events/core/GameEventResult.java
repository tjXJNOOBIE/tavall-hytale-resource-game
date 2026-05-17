package com.tavall.resourcegame.events.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record GameEventResult(
        UUID eventId,
        GameEventType eventType,
        boolean successful,
        boolean cancelled,
        Optional<String> failureReason,
        List<GameEvent> emittedEvents,
        List<String> dirtyFields,
        Map<String, String> metadata
) {
    public GameEventResult {
        failureReason = failureReason == null ? Optional.empty() : failureReason;
        emittedEvents = emittedEvents == null ? List.of() : List.copyOf(emittedEvents);
        dirtyFields = dirtyFields == null ? List.of() : List.copyOf(dirtyFields);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static GameEventResult successful(GameEvent event, List<GameEvent> emittedEvents, List<String> dirtyFields, Map<String, String> metadata) {
        return new GameEventResult(event.getEventId(), event.getEventType(), true, false, Optional.empty(), emittedEvents, dirtyFields, metadata);
    }

    public static GameEventResult cancelled(GameEvent event, String reason, Map<String, String> metadata) {
        return new GameEventResult(event.getEventId(), event.getEventType(), false, true, Optional.ofNullable(reason), List.of(), List.of(), metadata);
    }

    public static GameEventResult failed(GameEvent event, String reason, Map<String, String> metadata) {
        return new GameEventResult(event.getEventId(), event.getEventType(), false, false, Optional.ofNullable(reason), List.of(), List.of(), metadata);
    }
}
