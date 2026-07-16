package org.tavall.control.events.middleware;

import org.tavall.control.events.core.GameEventContext;
import org.tavall.control.events.core.GameEventResult;

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
