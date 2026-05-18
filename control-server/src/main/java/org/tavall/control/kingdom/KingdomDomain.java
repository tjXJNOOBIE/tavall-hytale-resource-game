package org.tavall.control.kingdom;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.control.event.DomainEventPublisher;
import org.tavall.control.event.RecordingDomainEventPublisher;

public interface KingdomDomain {
    default UniversalKingdomSimulationSystem.UniversalKingdomRepository getUniversalKingdomRepository() {
        return DependencyLoaderAccess.findOptionalInstance(UniversalKingdomSimulationSystem.UniversalKingdomRepository.class)
                .orElseGet(() -> registerUniversalKingdomRepository(new UniversalKingdomSimulationSystem.InMemoryUniversalKingdomRepository()));
    }

    default DomainEventPublisher getDomainEventPublisher() {
        return DependencyLoaderAccess.findOptionalInstance(DomainEventPublisher.class)
                .orElseGet(() -> registerDomainEventPublisher(new RecordingDomainEventPublisher()));
    }

    default UniversalKingdomSimulationSystem getUniversalKingdomSimulationSystem() {
        return DependencyLoaderAccess.findOptionalInstance(UniversalKingdomSimulationSystem.class)
                .orElseGet(() -> registerUniversalKingdomSimulationSystem(new UniversalKingdomSimulationSystem()));
    }

    default UniversalKingdomSimulationSystem.UniversalKingdomRepository registerUniversalKingdomRepository(UniversalKingdomSimulationSystem.UniversalKingdomRepository repository) {
        DependencyLoaderAccess.registerInstance(UniversalKingdomSimulationSystem.UniversalKingdomRepository.class, repository);
        return repository;
    }

    default DomainEventPublisher registerDomainEventPublisher(DomainEventPublisher eventPublisher) {
        DependencyLoaderAccess.registerInstance(DomainEventPublisher.class, eventPublisher);
        return eventPublisher;
    }

    default UniversalKingdomSimulationSystem registerUniversalKingdomSimulationSystem(UniversalKingdomSimulationSystem system) {
        DependencyLoaderAccess.registerInstance(UniversalKingdomSimulationSystem.class, system);
        return system;
    }
}
