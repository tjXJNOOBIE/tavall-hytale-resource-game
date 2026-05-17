package com.tavall.resourcegame.events.core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class GameEventContext {
    private final GameEvent event;
    private final Instant startedAt;
    private final Map<String, String> metadata = new LinkedHashMap<>();
    private final List<GameEvent> emittedEvents = new ArrayList<>();
    private final LinkedHashSet<String> dirtyFields = new LinkedHashSet<>();
    private boolean cancelled;
    private String failureReason;

    public GameEventContext(GameEvent event, Instant startedAt) {
        this.event = event;
        this.startedAt = startedAt;
    }

    public GameEvent event() {
        return event;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Map<String, String> metadata() {
        return metadata;
    }

    public List<GameEvent> emittedEvents() {
        return List.copyOf(emittedEvents);
    }

    public List<String> dirtyFields() {
        return List.copyOf(dirtyFields);
    }

    public boolean cancelled() {
        return cancelled;
    }

    public String failureReason() {
        return failureReason;
    }

    public void putMetadata(String key, String value) {
        if (key != null && !key.isBlank() && value != null) {
            metadata.put(key, value);
        }
    }

    public void emit(GameEvent emittedEvent) {
        if (emittedEvent != null) {
            emittedEvents.add(emittedEvent);
        }
    }

    public void markDirty(String dirtyField) {
        if (dirtyField != null && !dirtyField.isBlank()) {
            dirtyFields.add(dirtyField);
        }
    }

    public void cancel(String reason) {
        this.cancelled = true;
        this.failureReason = reason == null || reason.isBlank() ? "Event was cancelled." : reason;
    }

    public GameEventResult result(boolean successful) {
        if (cancelled) {
            return GameEventResult.cancelled(event, failureReason, metadata);
        }
        return successful
                ? GameEventResult.successful(event, emittedEvents, dirtyFields(), metadata)
                : GameEventResult.failed(event, failureReason, metadata);
    }
}
