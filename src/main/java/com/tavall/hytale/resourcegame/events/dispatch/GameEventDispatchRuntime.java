package com.tavall.hytale.resourcegame.events.dispatch;

import com.tavall.hytale.resourcegame.events.middleware.EventDispatchMetricsMiddleware;
import com.tavall.hytale.resourcegame.events.middleware.GameEventAuditHandler;

public record GameEventDispatchRuntime(
        GameEventDispatchHandler dispatchHandler,
        GameEventListenerRegistry listenerRegistry,
        GameEventAuditHandler auditHandler,
        EventDispatchMetricsMiddleware metricsMiddleware,
        InteractionBridgeHandler interactionBridgeHandler
) {
}
