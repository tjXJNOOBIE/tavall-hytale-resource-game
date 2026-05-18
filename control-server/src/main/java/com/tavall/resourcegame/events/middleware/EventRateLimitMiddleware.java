package org.tavall.control.events.middleware;

import org.tavall.control.events.IGameEventDomain;
import org.tavall.control.events.core.GameEvent;
import org.tavall.control.events.core.GameEventContext;
import org.tavall.control.events.core.GameEventResult;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EventRateLimitMiddleware implements GameEventMiddleware, IGameEventDomain {
    private final Map<String, Deque<Long>> recentEventsByActorAndType = new ConcurrentHashMap<>();

    @Override
    public GameEventResult handle(GameEventContext context, GameEventMiddlewareChain chain) {
        GameEvent event = context.event();
        String key = event.getActorId() + ":" + event.getEventType();
        long now = getEventRateLimitPolicy().clock().millis();
        Deque<Long> timestamps = recentEventsByActorAndType.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (timestamps) {
            long cutoff = now - getEventRateLimitPolicy().window().toMillis();
            while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
                timestamps.removeFirst();
            }
            if (timestamps.size() >= getEventRateLimitPolicy().maxEvents()) {
                context.cancel("Event rate limit exceeded for " + event.getEventType() + ".");
                return context.result(false);
            }
            timestamps.addLast(now);
        }
        return chain.proceed(context);
    }
}
