package com.tavall.resourcegame.events.middleware;

import com.tavall.resourcegame.events.IGameEventDomain;
import com.tavall.resourcegame.events.core.EventSource;
import com.tavall.resourcegame.events.core.GameEventContext;
import com.tavall.resourcegame.events.core.GameEventResult;

public final class EventDistributedForwardingMiddleware implements GameEventMiddleware, IGameEventDomain {
    @Override
    public GameEventResult handle(GameEventContext context, GameEventMiddlewareChain chain) {
        GameEventResult result = chain.proceed(context);
        if (result.successful() && context.event().getSource() != EventSource.REMOTE_NODE) {
            getDistributedEventForwarder().forward(context.event());
            context.putMetadata("distributedForwarded", "true");
        }
        return result;
    }
}
