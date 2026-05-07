package com.tavall.hytale.resourcegame.events.middleware;

import com.tavall.hytale.resourcegame.events.core.GameEvent;

@FunctionalInterface
public interface DistributedEventForwarder {
    void forward(GameEvent event);
}
