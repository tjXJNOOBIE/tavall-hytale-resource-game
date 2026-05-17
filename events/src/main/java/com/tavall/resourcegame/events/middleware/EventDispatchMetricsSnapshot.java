package com.tavall.resourcegame.events.middleware;

import java.util.Map;

public record EventDispatchMetricsSnapshot(
        long dispatched,
        long successful,
        long cancelled,
        long failed,
        Map<String, Long> dispatchedByType
) {
    public EventDispatchMetricsSnapshot {
        dispatchedByType = dispatchedByType == null ? Map.of() : Map.copyOf(dispatchedByType);
    }
}
