package org.tavall.control.events.dispatch;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.events.IGameEventDomain;
import org.tavall.control.events.middleware.EventDispatchMetricsMiddleware;
import org.tavall.control.events.middleware.GameEventAuditHandler;

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
