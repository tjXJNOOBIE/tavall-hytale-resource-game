package com.tavall.hytale.resourcegame.events.middleware;

import java.util.ArrayList;
import java.util.List;

public final class GameEventAuditHandler {
    private final List<EventAuditEntry> entries = new ArrayList<>();

    public synchronized void record(EventAuditEntry entry) {
        entries.add(entry);
    }

    public synchronized List<EventAuditEntry> entries() {
        return List.copyOf(entries);
    }
}
