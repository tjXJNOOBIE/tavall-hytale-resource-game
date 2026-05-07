package com.tavall.hytale.resourcegame.events.middleware;

import com.tavall.hytale.resourcegame.events.core.GameEvent;
import com.tavall.hytale.resourcegame.events.core.GameEventContext;
import com.tavall.hytale.resourcegame.events.core.GameEventResult;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class EventRateLimitMiddleware implements GameEventMiddleware {
    private final int maxEvents;
    private final Duration window;
    private final Clock clock;
    private final Map<String, Deque<Long>> recentEventsByActorAndType = new ConcurrentHashMap<>();

    public EventRateLimitMiddleware(int maxEvents, Duration window, Clock clock) {
        if (maxEvents <= 0) {
            throw new IllegalArgumentException("maxEvents must be positive.");
        }
        this.maxEvents = maxEvents;
        this.window = Objects.requireNonNull(window, "window");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public GameEventResult handle(GameEventContext context, GameEventMiddlewareChain chain) {
        GameEvent event = context.event();
        String key = event.getActorId() + ":" + event.getEventType();
        long now = clock.millis();
        Deque<Long> timestamps = recentEventsByActorAndType.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (timestamps) {
            long cutoff = now - window.toMillis();
            while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
                timestamps.removeFirst();
            }
            if (timestamps.size() >= maxEvents) {
                context.cancel("Event rate limit exceeded for " + event.getEventType() + ".");
                return context.result(false);
            }
            timestamps.addLast(now);
        }
        return chain.proceed(context);
    }
}
