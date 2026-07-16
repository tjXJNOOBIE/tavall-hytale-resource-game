package org.tavall.control.events.middleware;

import org.tavall.control.events.core.GameEvent;

@FunctionalInterface
public interface DistributedEventForwarder {
    void forward(GameEvent event);
}
