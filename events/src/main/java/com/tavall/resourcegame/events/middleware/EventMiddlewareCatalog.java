package com.tavall.resourcegame.events.middleware;

import java.util.List;

public record EventMiddlewareCatalog(List<GameEventMiddleware> middlewares) {
    public EventMiddlewareCatalog {
        middlewares = middlewares == null ? List.of() : List.copyOf(middlewares);
    }
}
