package org.tavall.control.events.middleware;

import org.tavall.control.events.GameEventDomain;
import org.tavall.control.events.core.EventSource;
import org.tavall.control.events.core.GameEventContext;
import org.tavall.control.events.core.GameEventResult;

public final class EventDistributedForwardingMiddleware implements GameEventMiddleware, GameEventDomain {
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
