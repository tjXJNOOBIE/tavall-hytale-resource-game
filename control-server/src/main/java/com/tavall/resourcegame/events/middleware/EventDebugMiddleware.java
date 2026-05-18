package org.tavall.control.events.middleware;

import org.tavall.control.events.core.GameEventContext;
import org.tavall.control.events.core.GameEventResult;

public final class EventDebugMiddleware implements GameEventMiddleware {
    @Override
    public GameEventResult handle(GameEventContext context, GameEventMiddlewareChain chain) {
        context.putMetadata("eventSource", context.event().getSource().name());
        context.putMetadata("eventType", context.event().getEventType().name());
        return chain.proceed(context);
    }
}
