package com.tavall.hytale.resourcegame.events.middleware;

import com.tavall.hytale.resourcegame.events.core.GameEvent;

public final class NoopDistributedEventForwarder implements DistributedEventForwarder {
    @Override
    public void forward(GameEvent event) {
    }
}
