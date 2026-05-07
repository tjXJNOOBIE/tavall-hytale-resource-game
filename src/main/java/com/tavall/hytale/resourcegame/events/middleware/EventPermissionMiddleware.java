package com.tavall.hytale.resourcegame.events.middleware;

import com.tavall.hytale.resourcegame.events.core.GameEventContext;
import com.tavall.hytale.resourcegame.events.core.GameEventResult;

import java.util.Objects;

public final class EventPermissionMiddleware implements GameEventMiddleware {
    private final EventPermissionPolicy permissionPolicy;

    public EventPermissionMiddleware(EventPermissionPolicy permissionPolicy) {
        this.permissionPolicy = Objects.requireNonNull(permissionPolicy, "permissionPolicy");
    }

    @Override
    public GameEventResult handle(GameEventContext context, GameEventMiddlewareChain chain) {
        if (!permissionPolicy.canDispatch(context.event())) {
            context.cancel("Actor is not allowed to dispatch " + context.event().getEventType() + ".");
            return context.result(false);
        }
        return chain.proceed(context);
    }
}
