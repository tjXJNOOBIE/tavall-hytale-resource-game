package com.tavall.hytale.resourcegame.events.middleware;

import com.tavall.hytale.resourcegame.events.core.GameEventContext;
import com.tavall.hytale.resourcegame.events.core.GameEventResult;

import java.util.List;

public final class GameEventMiddlewareChain {
    private final List<GameEventMiddleware> middlewares;
    private final GameEventTerminalHandler terminalHandler;
    private final int index;

    public GameEventMiddlewareChain(List<GameEventMiddleware> middlewares, GameEventTerminalHandler terminalHandler) {
        this(middlewares, terminalHandler, 0);
    }

    private GameEventMiddlewareChain(List<GameEventMiddleware> middlewares, GameEventTerminalHandler terminalHandler, int index) {
        this.middlewares = middlewares == null ? List.of() : List.copyOf(middlewares);
        this.terminalHandler = terminalHandler;
        this.index = index;
    }

    public GameEventResult proceed(GameEventContext context) {
        if (index >= middlewares.size()) {
            return terminalHandler.handle(context);
        }
        GameEventMiddleware middleware = middlewares.get(index);
        return middleware.handle(context, new GameEventMiddlewareChain(middlewares, terminalHandler, index + 1));
    }
}
