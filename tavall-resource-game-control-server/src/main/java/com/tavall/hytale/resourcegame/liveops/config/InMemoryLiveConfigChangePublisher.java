package com.tavall.hytale.resourcegame.liveops.config;

import java.util.ArrayList;
import java.util.List;

public final class InMemoryLiveConfigChangePublisher implements LiveConfigChangePublisher {
    private final List<LiveConfigChange> changes = new ArrayList<>();

    @Override
    public synchronized void publish(LiveConfigChange change) {
        changes.add(change);
    }

    public synchronized List<LiveConfigChange> changes() {
        return List.copyOf(changes);
    }
}
