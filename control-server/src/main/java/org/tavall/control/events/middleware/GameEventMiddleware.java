package org.tavall.control.events.middleware;

import org.tavall.control.events.core.GameEventContext;
import org.tavall.control.events.core.GameEventResult;

@FunctionalInterface
public interface GameEventMiddleware {
    GameEventResult handle(GameEventContext context, GameEventMiddlewareChain chain);
}
