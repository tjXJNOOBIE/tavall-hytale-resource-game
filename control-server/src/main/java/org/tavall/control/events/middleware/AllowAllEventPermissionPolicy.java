package org.tavall.control.events.middleware;

import org.tavall.control.events.core.GameEvent;

public final class AllowAllEventPermissionPolicy implements EventPermissionPolicy {
    @Override
    public boolean canDispatch(GameEvent event) {
        return true;
    }
}
