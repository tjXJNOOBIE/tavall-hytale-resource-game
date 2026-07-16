package org.tavall.control.events.dispatch;

import org.tavall.control.events.GameEventDomain;
import org.tavall.control.events.core.GameEvent;
import org.tavall.control.events.core.GameEventContext;
import org.tavall.control.events.core.GameEventResult;
import org.tavall.control.events.middleware.GameEventMiddlewareChain;

import java.time.Instant;
import java.util.Objects;

public final class GameEventDispatchHandler implements GameEventDomain {
    public GameEventResult dispatch(GameEvent event) {
        Objects.requireNonNull(event, "event");
        GameEventContext context = new GameEventContext(event, Instant.now());
        try {
            GameEventMiddlewareChain chain = GameEventMiddlewareChain.start(getEventMiddlewareCatalog().middlewares(), this::dispatchToListeners);
            return chain.proceed(context);
        } catch (RuntimeException ex) {
            context.cancel(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
            context.putMetadata("exceptionType", ex.getClass().getSimpleName());
            return context.result(false);
        }
    }

    private GameEventResult dispatchToListeners(GameEventContext context) {
        for (GameEventListener listener : getGameEventListenerRegistry().listenersFor(context.event().getEventType())) {
            if (context.cancelled()) {
                return context.result(false);
            }
            if (listener.supports(context.event().getEventType())) {
                listener.handle(context);
            }
        }
        return context.result(true);
    }
}
