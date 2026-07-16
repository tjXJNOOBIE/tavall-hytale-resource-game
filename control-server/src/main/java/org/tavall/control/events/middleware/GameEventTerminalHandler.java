package org.tavall.control.events.middleware;

import org.tavall.control.events.core.GameEventContext;
import org.tavall.control.events.core.GameEventResult;

@FunctionalInterface
public interface GameEventTerminalHandler {
    GameEventResult handle(GameEventContext context);
}
