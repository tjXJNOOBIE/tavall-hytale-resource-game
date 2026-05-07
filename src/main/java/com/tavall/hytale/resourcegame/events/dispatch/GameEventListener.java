package com.tavall.hytale.resourcegame.events.dispatch;

import com.tavall.hytale.resourcegame.events.core.GameEventContext;
import com.tavall.hytale.resourcegame.events.core.GameEventType;

@FunctionalInterface
public interface GameEventListener {
    void handle(GameEventContext context);

    default boolean supports(GameEventType eventType) {
        return true;
    }
}
