package com.tavall.hytale.resourcegame.events.middleware;

import com.tavall.hytale.resourcegame.events.core.GameEventContext;
import com.tavall.hytale.resourcegame.events.core.GameEventResult;

public final class EventCancellationMiddleware implements GameEventMiddleware {
    @Override
    public GameEventResult handle(GameEventContext context, GameEventMiddlewareChain chain) {
        if (context.cancelled()) {
            return context.result(false);
        }
        GameEventResult result = chain.proceed(context);
        if (context.cancelled()) {
            return context.result(false);
        }
        return result;
    }
}
