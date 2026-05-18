package org.tavall.control.event;

public interface DomainEventPublisher {
    void publish(DomainEvent domainEvent);
}
