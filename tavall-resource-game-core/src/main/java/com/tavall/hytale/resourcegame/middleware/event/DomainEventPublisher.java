package com.tavall.hytale.resourcegame.middleware.event;

public interface DomainEventPublisher {
    void publish(DomainEvent domainEvent);
}
