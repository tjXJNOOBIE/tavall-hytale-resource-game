package com.tavall.hytale.resourcegame.events.middleware;

import com.tavall.hytale.resourcegame.events.core.GameEventContext;
import com.tavall.hytale.resourcegame.events.core.GameEventResult;

import java.util.Objects;
import java.util.logging.Logger;

public final class EventLoggingMiddleware implements GameEventMiddleware {
    private final Logger logger;

    public EventLoggingMiddleware(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public GameEventResult handle(GameEventContext context, GameEventMiddlewareChain chain) {
        logger.fine(() -> "Dispatching backend game event " + context.event().getEventType() + " from " + context.event().getSource());
        GameEventResult result = chain.proceed(context);
        logger.fine(() -> "Backend game event " + context.event().getEventType() + " completed successful=" + result.successful());
        return result;
    }
}
