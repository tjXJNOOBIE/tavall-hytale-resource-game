package com.tavall.resourcegame.events.middleware;

import com.tavall.resourcegame.events.core.GameEventContext;
import com.tavall.resourcegame.events.core.GameEventResult;

import java.util.List;
import java.util.Objects;

public final class GameEventMiddlewareChain {
    private final List<GameEventMiddleware> middlewares;
    private final GameEventTerminalHandler terminalHandler;
    private final int index;

    private GameEventMiddlewareChain(
            List<GameEventMiddleware> middlewares,
            GameEventTerminalHandler terminalHandler,
            int index
    ) {
        this.middlewares = List.copyOf(middlewares);
        this.terminalHandler = Objects.requireNonNull(terminalHandler, "terminalHandler");
        this.index = index;
    }

    public static GameEventMiddlewareChain start(
            List<GameEventMiddleware> middlewares,
            GameEventTerminalHandler terminalHandler
    ) {
        return new GameEventMiddlewareChain(middlewares == null ? List.of() : middlewares, terminalHandler, 0);
    }

    /**
     * Each proceed call advances to a new immutable chain instance so middleware cannot re-run
     * earlier stages or skip the terminal listener dispatch by mutating shared cursor state.
     */
    public GameEventResult proceed(GameEventContext context) {
        Objects.requireNonNull(context, "context");
        if (context.cancelled()) {
            return context.result(false);
        }
        if (index >= middlewares.size()) {
            return terminalHandler.handle(context);
        }
        GameEventMiddleware middleware = middlewares.get(index);
        return middleware.handle(context, new GameEventMiddlewareChain(middlewares, terminalHandler, index + 1));
    }
}
