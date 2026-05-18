package org.tavall.control.events.middleware;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.events.core.GameEventContext;
import org.tavall.control.events.core.GameEventResult;

import java.util.logging.Logger;

public final class EventLoggingMiddleware implements GameEventMiddleware, IDependencyInjectableConcrete {
    private static final Logger LOGGER = Logger.getLogger(EventLoggingMiddleware.class.getName());

    @Override
    public GameEventResult handle(GameEventContext context, GameEventMiddlewareChain chain) {
        LOGGER.fine(() -> "Dispatching backend game event " + context.event().getEventType() + " from " + context.event().getSource());
        GameEventResult result = chain.proceed(context);
        LOGGER.fine(() -> "Backend game event " + context.event().getEventType() + " completed successful=" + result.successful());
        return result;
    }
}
