package org.tavall.control.events.middleware;

import org.tavall.control.events.core.GameEvent;

public final class NoopDistributedEventForwarder implements DistributedEventForwarder {
    @Override
    public void forward(GameEvent event) {
    }
}
