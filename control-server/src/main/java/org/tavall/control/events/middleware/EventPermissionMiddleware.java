package org.tavall.control.events.middleware;

import org.tavall.control.events.IGameEventDomain;
import org.tavall.control.events.core.GameEventContext;
import org.tavall.control.events.core.GameEventResult;

public final class EventPermissionMiddleware implements GameEventMiddleware, IGameEventDomain {
    @Override
    public GameEventResult handle(GameEventContext context, GameEventMiddlewareChain chain) {
        if (!getEventPermissionPolicy().canDispatch(context.event())) {
            context.cancel("Actor is not allowed to dispatch " + context.event().getEventType() + ".");
            return context.result(false);
        }
        return chain.proceed(context);
    }
}
