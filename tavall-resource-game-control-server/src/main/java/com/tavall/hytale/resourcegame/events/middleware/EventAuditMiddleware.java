package com.tavall.hytale.resourcegame.events.middleware;

import com.tavall.hytale.resourcegame.events.core.GameEvent;
import com.tavall.hytale.resourcegame.events.core.GameEventContext;
import com.tavall.hytale.resourcegame.events.core.GameEventResult;

import java.time.Instant;
import java.util.Objects;

public final class EventAuditMiddleware implements GameEventMiddleware {
    private final GameEventAuditHandler auditHandler;

    public EventAuditMiddleware(GameEventAuditHandler auditHandler) {
        this.auditHandler = Objects.requireNonNull(auditHandler, "auditHandler");
    }

    @Override
    public GameEventResult handle(GameEventContext context, GameEventMiddlewareChain chain) {
        GameEventResult result = chain.proceed(context);
        GameEvent event = context.event();
        auditHandler.record(new EventAuditEntry(
                event.getEventId(),
                event.getEventType(),
                event.getActorId(),
                event.getSource(),
                result.successful(),
                result.cancelled(),
                result.failureReason().orElse(""),
                Instant.now(),
                result.metadata()
        ));
        return result;
    }
}
