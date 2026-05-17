package com.tavall.resourcegame.events.middleware;

import com.tavall.resourcegame.events.core.EventSource;
import com.tavall.resourcegame.events.core.GameEventType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record EventAuditEntry(
        UUID eventId,
        GameEventType eventType,
        UUID actorId,
        EventSource source,
        boolean successful,
        boolean cancelled,
        String failureReason,
        Instant recordedAt,
        Map<String, String> metadata
) {
    public EventAuditEntry {
        failureReason = failureReason == null ? "" : failureReason;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
