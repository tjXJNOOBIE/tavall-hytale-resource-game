package com.tavall.resourcegame.events.middleware;

import com.tavall.resourcegame.events.IGameEventDomain;
import com.tavall.resourcegame.events.core.GameEventContext;
import com.tavall.resourcegame.events.core.GameEventResult;

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
