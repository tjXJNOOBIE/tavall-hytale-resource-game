package com.tavall.resourcegame.events.middleware;

import com.tavall.resourcegame.events.core.GameEvent;

@FunctionalInterface
public interface EventPermissionPolicy {
    boolean canDispatch(GameEvent event);
}
