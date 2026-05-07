package com.tavall.hytale.resourcegame.liveops.gui;

import java.util.ArrayList;
import java.util.List;

public final class InMemoryGlobalGuiChangePublisher implements GlobalGuiChangePublisher {
    private final List<GlobalGuiChange> changes = new ArrayList<>();

    @Override
    public synchronized void publish(GlobalGuiChange change) {
        changes.add(change);
    }

    public synchronized List<GlobalGuiChange> changes() {
        return List.copyOf(changes);
    }
}
