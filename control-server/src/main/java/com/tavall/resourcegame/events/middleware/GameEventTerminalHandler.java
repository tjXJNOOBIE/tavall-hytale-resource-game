package com.tavall.resourcegame.events.middleware;

import com.tavall.resourcegame.events.core.GameEventContext;
import com.tavall.resourcegame.events.core.GameEventResult;

@FunctionalInterface
public interface GameEventTerminalHandler {
    GameEventResult handle(GameEventContext context);
}
