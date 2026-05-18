package org.tavall.control.events.middleware;

import org.tavall.control.events.core.GameEvent;

@FunctionalInterface
public interface EventPermissionPolicy {
    boolean canDispatch(GameEvent event);
}
