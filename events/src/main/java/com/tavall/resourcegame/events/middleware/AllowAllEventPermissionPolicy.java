package com.tavall.resourcegame.events.middleware;

import com.tavall.resourcegame.events.core.GameEvent;

public final class AllowAllEventPermissionPolicy implements EventPermissionPolicy {
    @Override
    public boolean canDispatch(GameEvent event) {
        return true;
    }
}
