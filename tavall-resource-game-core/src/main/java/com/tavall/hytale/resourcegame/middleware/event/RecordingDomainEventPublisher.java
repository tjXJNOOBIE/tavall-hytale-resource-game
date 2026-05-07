package com.tavall.hytale.resourcegame.middleware.event;

import java.util.ArrayList;
import java.util.List;

public final class RecordingDomainEventPublisher implements DomainEventPublisher {
    private final List<DomainEvent> publishedEvents = new ArrayList<>();

    @Override
    public void publish(DomainEvent domainEvent) {
        publishedEvents.add(domainEvent);
    }

    public List<DomainEvent> publishedEvents() {
        return List.copyOf(publishedEvents);
    }
}
