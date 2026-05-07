package com.tavall.hytale.resourcegame.events.middleware;

import com.tavall.hytale.resourcegame.events.core.GameEventContext;
import com.tavall.hytale.resourcegame.events.core.GameEventResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class EventDispatchMetricsMiddleware implements GameEventMiddleware {
    private final AtomicLong dispatched = new AtomicLong();
    private final AtomicLong successful = new AtomicLong();
    private final AtomicLong cancelled = new AtomicLong();
    private final AtomicLong failed = new AtomicLong();
    private final Map<String, AtomicLong> dispatchedByType = new ConcurrentHashMap<>();

    @Override
    public GameEventResult handle(GameEventContext context, GameEventMiddlewareChain chain) {
        dispatched.incrementAndGet();
        dispatchedByType.computeIfAbsent(context.event().getEventType().name(), ignored -> new AtomicLong()).incrementAndGet();
        GameEventResult result = chain.proceed(context);
        if (result.successful()) {
            successful.incrementAndGet();
        } else if (result.cancelled()) {
            cancelled.incrementAndGet();
        } else {
            failed.incrementAndGet();
        }
        context.putMetadata("eventDispatchCount", Long.toString(dispatched.get()));
        return result;
    }

    public EventDispatchMetricsSnapshot snapshot() {
        return new EventDispatchMetricsSnapshot(
                dispatched.get(),
                successful.get(),
                cancelled.get(),
                failed.get(),
                dispatchedByType.entrySet().stream().collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()))
        );
    }
}
