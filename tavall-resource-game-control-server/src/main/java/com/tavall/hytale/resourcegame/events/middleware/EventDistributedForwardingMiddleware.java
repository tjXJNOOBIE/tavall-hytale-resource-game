package com.tavall.hytale.resourcegame.events.middleware;

import com.tavall.hytale.resourcegame.events.core.EventSource;
import com.tavall.hytale.resourcegame.events.core.GameEventContext;
import com.tavall.hytale.resourcegame.events.core.GameEventResult;

import java.util.Objects;

public final class EventDistributedForwardingMiddleware implements GameEventMiddleware {
    private final DistributedEventForwarder forwarder;

    public EventDistributedForwardingMiddleware(DistributedEventForwarder forwarder) {
        this.forwarder = Objects.requireNonNull(forwarder, "forwarder");
    }

    @Override
    public GameEventResult handle(GameEventContext context, GameEventMiddlewareChain chain) {
        GameEventResult result = chain.proceed(context);
        if (result.successful() && context.event().getSource() != EventSource.REMOTE_NODE) {
            forwarder.forward(context.event());
            context.putMetadata("distributedForwarded", "true");
        }
        return result;
    }
}
