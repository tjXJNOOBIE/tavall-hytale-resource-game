package com.tavall.resourcegame.middleware.event;

public interface DomainEventPublisher {
    void publish(DomainEvent domainEvent);
}
