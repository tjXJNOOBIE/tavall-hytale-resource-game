package com.tavall.resourcegame.events.middleware;

import com.tavall.resourcegame.events.core.GameEvent;

public final class NoopDistributedEventForwarder implements DistributedEventForwarder {
    @Override
    public void forward(GameEvent event) {
    }
}
