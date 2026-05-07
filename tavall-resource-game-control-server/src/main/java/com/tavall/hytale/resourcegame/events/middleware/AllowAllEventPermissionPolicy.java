package com.tavall.hytale.resourcegame.events.middleware;

import com.tavall.hytale.resourcegame.events.core.GameEvent;

public final class AllowAllEventPermissionPolicy implements EventPermissionPolicy {
    @Override
    public boolean canDispatch(GameEvent event) {
        return true;
    }
}
