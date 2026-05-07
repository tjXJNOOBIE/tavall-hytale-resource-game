package com.tavall.hytale.resourcegame.events.dispatch;

import com.tavall.hytale.resourcegame.events.core.GameEvent;
import com.tavall.hytale.resourcegame.events.core.GameEventContext;
import com.tavall.hytale.resourcegame.events.core.GameEventResult;
import com.tavall.hytale.resourcegame.events.middleware.GameEventMiddleware;
import com.tavall.hytale.resourcegame.events.middleware.GameEventMiddlewareChain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class GameEventDispatchHandler {
    private final List<GameEventMiddleware> middlewares;
    private final GameEventListenerRegistry listenerRegistry;

    public GameEventDispatchHandler(List<GameEventMiddleware> middlewares, GameEventListenerRegistry listenerRegistry) {
        this.middlewares = middlewares == null ? List.of() : List.copyOf(middlewares);
        this.listenerRegistry = Objects.requireNonNull(listenerRegistry, "listenerRegistry");
    }

    public GameEventResult dispatch(GameEvent event) {
        Objects.requireNonNull(event, "event");
        GameEventContext context = new GameEventContext(event, Instant.now());
        try {
            GameEventMiddlewareChain chain = new GameEventMiddlewareChain(middlewares, this::dispatchToListeners);
            return chain.proceed(context);
        } catch (RuntimeException ex) {
            context.cancel(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
            context.putMetadata("exceptionType", ex.getClass().getSimpleName());
            return context.result(false);
        }
    }

    private GameEventResult dispatchToListeners(GameEventContext context) {
        for (GameEventListener listener : listenerRegistry.listenersFor(context.event().getEventType())) {
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
