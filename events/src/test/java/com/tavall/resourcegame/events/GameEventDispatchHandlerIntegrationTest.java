package com.tavall.resourcegame.events;

import com.tavall.resourcegame.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.events.core.BasicGameEvent;
import com.tavall.resourcegame.events.core.EventSource;
import com.tavall.resourcegame.events.core.GameEventResult;
import com.tavall.resourcegame.events.core.GameEventType;
import com.tavall.resourcegame.events.dispatch.FrontendInteractionRequest;
import com.tavall.resourcegame.events.dispatch.GameEventDispatchHandler;
import com.tavall.resourcegame.events.dispatch.GameEventDispatchRuntime;
import com.tavall.resourcegame.events.dispatch.GameEventDispatchRuntimeFactory;
import com.tavall.resourcegame.events.dispatch.GameEventListenerRegistry;
import com.tavall.resourcegame.events.middleware.EventAuditMiddleware;
import com.tavall.resourcegame.events.middleware.EventMiddlewareCatalog;
import com.tavall.resourcegame.events.middleware.EventPermissionMiddleware;
import com.tavall.resourcegame.events.middleware.EventPermissionPolicy;
import com.tavall.resourcegame.events.middleware.EventValidationMiddleware;
import com.tavall.resourcegame.events.middleware.GameEventAuditHandler;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class GameEventDispatchHandlerIntegrationTest {
    @Test
    void frontendInteractionSubmitsBackendGameEventThroughMiddlewareAndListeners() {
        DependencyLoaderAccess.clear();
        GameEventDispatchRuntime runtime = GameEventDispatchRuntimeFactory.createInMemoryRuntime();
        runtime.listenerRegistry().register(GameEventType.CASTLE_SELECTED, context -> {
            context.markDirty("castle.selection");
            context.emit(new BasicGameEvent(GameEventType.CASTLE_VISUAL_UPDATED, context.event().getActorId(), EventSource.SYSTEM_TICK, Map.of("reason", "selection")));
        });
        UUID actorId = UUID.randomUUID();
        FrontendInteractionRequest request = new FrontendInteractionRequest(
                UUID.randomUUID(),
                actorId,
                "castle.select",
                "castle-1",
                Instant.parse("2026-05-07T12:15:00Z"),
                Map.of("selectionMode", "single")
        );

        GameEventResult result = runtime.interactionBridgeHandler().submitInteraction(request);

        assertTrue(result.successful());
        assertEquals(GameEventType.CASTLE_SELECTED, result.eventType());
        assertEquals(List.of("castle.selection"), result.dirtyFields());
        assertEquals(GameEventType.CASTLE_VISUAL_UPDATED, result.emittedEvents().getFirst().getEventType());
        assertEquals(1, runtime.auditHandler().entries().size());
        assertEquals(1L, runtime.metricsMiddleware().snapshot().successful());
    }

    @Test
    void permissionMiddlewareCancelsEventBeforeStateListenerRuns() {
        DependencyLoaderAccess.clear();
        GameEventListenerRegistry listenerRegistry = new GameEventListenerRegistry();
        GameEventAuditHandler auditHandler = new GameEventAuditHandler();
        AtomicBoolean listenerCalled = new AtomicBoolean(false);
        listenerRegistry.register(GameEventType.CITIZEN_CREATED, context -> listenerCalled.set(true));
        DependencyLoaderAccess.registerInstance(GameEventListenerRegistry.class, listenerRegistry);
        DependencyLoaderAccess.registerInstance(GameEventAuditHandler.class, auditHandler);
        DependencyLoaderAccess.registerInstance(EventPermissionPolicy.class, event -> false);
        new GameEventDependencyModule().registerDependencies();
        DependencyLoaderAccess.registerInstance(EventMiddlewareCatalog.class, new EventMiddlewareCatalog(List.of(
                new EventValidationMiddleware(),
                new EventPermissionMiddleware(),
                new EventAuditMiddleware()
        )));
        GameEventDispatchHandler dispatchHandler = DependencyLoaderAccess.findInstance(GameEventDispatchHandler.class);

        GameEventResult result = dispatchHandler.dispatch(new BasicGameEvent(GameEventType.CITIZEN_CREATED, UUID.randomUUID(), EventSource.FRONTEND_INTERACTION, Map.of()));

        assertFalse(result.successful());
        assertTrue(result.cancelled());
        assertFalse(listenerCalled.get());
        assertTrue(result.failureReason().orElseThrow().contains("not allowed"));
    }
}
