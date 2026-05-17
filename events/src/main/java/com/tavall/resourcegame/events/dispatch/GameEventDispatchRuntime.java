package com.tavall.resourcegame.events.dispatch;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.events.IGameEventDomain;
import com.tavall.resourcegame.events.middleware.EventDispatchMetricsMiddleware;
import com.tavall.resourcegame.events.middleware.GameEventAuditHandler;

public final class GameEventDispatchRuntime implements IGameEventDomain, IDependencyInjectableConcrete {
    public GameEventDispatchHandler dispatchHandler() {
        return getGameEventDispatchHandler();
    }

    public GameEventListenerRegistry listenerRegistry() {
        return getGameEventListenerRegistry();
    }

    public GameEventAuditHandler auditHandler() {
        return getGameEventAuditHandler();
    }

    public EventDispatchMetricsMiddleware metricsMiddleware() {
        return getEventDispatchMetricsMiddleware();
    }

    public InteractionBridgeHandler interactionBridgeHandler() {
        return getInteractionBridgeHandler();
    }
}
