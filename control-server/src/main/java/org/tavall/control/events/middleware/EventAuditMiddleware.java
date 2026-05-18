package org.tavall.control.events.middleware;

import org.tavall.control.events.IGameEventDomain;
import org.tavall.control.events.core.GameEvent;
import org.tavall.control.events.core.GameEventContext;
import org.tavall.control.events.core.GameEventResult;

import java.time.Instant;

public final class EventAuditMiddleware implements GameEventMiddleware, IGameEventDomain {
    @Override
    public GameEventResult handle(GameEventContext context, GameEventMiddlewareChain chain) {
        GameEventResult result = chain.proceed(context);
        GameEvent event = context.event();
        getGameEventAuditHandler().record(new EventAuditEntry(
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
