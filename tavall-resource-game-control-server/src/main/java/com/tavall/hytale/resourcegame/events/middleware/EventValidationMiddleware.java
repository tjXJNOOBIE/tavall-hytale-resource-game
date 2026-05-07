package com.tavall.hytale.resourcegame.events.middleware;

import com.tavall.hytale.resourcegame.events.core.GameEvent;
import com.tavall.hytale.resourcegame.events.core.GameEventContext;
import com.tavall.hytale.resourcegame.events.core.GameEventResult;

public final class EventValidationMiddleware implements GameEventMiddleware {
    @Override
    public GameEventResult handle(GameEventContext context, GameEventMiddlewareChain chain) {
        GameEvent event = context.event();
        if (event.getEventId() == null) {
            context.cancel("Game event is missing an eventId.");
            return context.result(false);
        }
        if (event.getEventType() == null) {
            context.cancel("Game event is missing an eventType.");
            return context.result(false);
        }
        if (event.getActorId() == null) {
            context.cancel("Game event is missing an actorId.");
            return context.result(false);
        }
        if (event.getSource() == null) {
            context.cancel("Game event is missing a source.");
            return context.result(false);
        }
        if (event.getCreatedAtEpochMillis() <= 0L) {
            context.cancel("Game event has an invalid creation timestamp.");
            return context.result(false);
        }
        return chain.proceed(context);
    }
}
