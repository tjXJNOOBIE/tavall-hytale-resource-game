package com.tavall.hytale.resourcegame.events.middleware;

import com.tavall.hytale.resourcegame.events.core.GameEventContext;
import com.tavall.hytale.resourcegame.events.core.GameEventResult;

@FunctionalInterface
public interface GameEventTerminalHandler {
    GameEventResult handle(GameEventContext context);
}
