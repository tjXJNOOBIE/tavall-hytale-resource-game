package org.tavall.control.events.dispatch;

import org.tavall.control.events.core.GameEventContext;
import org.tavall.control.events.core.GameEventType;

@FunctionalInterface
public interface GameEventListener {
    void handle(GameEventContext context);

    default boolean supports(GameEventType eventType) {
        return true;
    }
}
