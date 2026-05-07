package com.tavall.hytale.resourcegame.events.middleware;

import com.tavall.hytale.resourcegame.events.core.GameEventContext;
import com.tavall.hytale.resourcegame.events.core.GameEventResult;

public final class EventDebugMiddleware implements GameEventMiddleware {
    @Override
    public GameEventResult handle(GameEventContext context, GameEventMiddlewareChain chain) {
        context.putMetadata("eventSource", context.event().getSource().name());
        context.putMetadata("eventType", context.event().getEventType().name());
        return chain.proceed(context);
    }
}
