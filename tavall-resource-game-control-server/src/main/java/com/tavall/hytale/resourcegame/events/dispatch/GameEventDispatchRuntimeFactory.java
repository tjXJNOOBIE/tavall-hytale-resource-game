package com.tavall.hytale.resourcegame.events.dispatch;

import com.tavall.hytale.resourcegame.events.middleware.AllowAllEventPermissionPolicy;
import com.tavall.hytale.resourcegame.events.middleware.EventAuditMiddleware;
import com.tavall.hytale.resourcegame.events.middleware.EventCancellationMiddleware;
import com.tavall.hytale.resourcegame.events.middleware.EventDebugMiddleware;
import com.tavall.hytale.resourcegame.events.middleware.EventDispatchMetricsMiddleware;
import com.tavall.hytale.resourcegame.events.middleware.EventDistributedForwardingMiddleware;
import com.tavall.hytale.resourcegame.events.middleware.EventPermissionMiddleware;
import com.tavall.hytale.resourcegame.events.middleware.EventRateLimitMiddleware;
import com.tavall.hytale.resourcegame.events.middleware.EventValidationMiddleware;
import com.tavall.hytale.resourcegame.events.middleware.GameEventAuditHandler;
import com.tavall.hytale.resourcegame.events.middleware.NoopDistributedEventForwarder;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

public final class GameEventDispatchRuntimeFactory {
    private GameEventDispatchRuntimeFactory() {
    }

    public static GameEventDispatchRuntime createInMemoryRuntime() {
        GameEventListenerRegistry listenerRegistry = new GameEventListenerRegistry();
        GameEventAuditHandler auditHandler = new GameEventAuditHandler();
        EventDispatchMetricsMiddleware metricsMiddleware = new EventDispatchMetricsMiddleware();
        GameEventDispatchHandler dispatchHandler = new GameEventDispatchHandler(
                List.of(
                        new EventValidationMiddleware(),
                        new EventCancellationMiddleware(),
                        new EventDebugMiddleware(),
                        new EventPermissionMiddleware(new AllowAllEventPermissionPolicy()),
                        new EventRateLimitMiddleware(120, Duration.ofSeconds(10), Clock.systemUTC()),
                        new EventAuditMiddleware(auditHandler),
                        metricsMiddleware,
                        new EventDistributedForwardingMiddleware(new NoopDistributedEventForwarder())
                ),
                listenerRegistry
        );
        return new GameEventDispatchRuntime(
                dispatchHandler,
                listenerRegistry,
                auditHandler,
                metricsMiddleware,
                new InteractionBridgeHandler(dispatchHandler)
        );
    }
}
