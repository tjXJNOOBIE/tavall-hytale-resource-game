package com.tavall.resourcegame.middleware.clock;

import com.tavall.resourcegame.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.middleware.event.DomainEventPublisher;
import com.tavall.resourcegame.middleware.event.RecordingDomainEventPublisher;

import java.time.Clock;

public interface IClockDomainGenerated {
    default KingdomClockRepository getKingdomClockRepository() {
        return DependencyLoaderAccess.findOptionalInstance(KingdomClockRepository.class)
                .orElseGet(() -> registerKingdomClockRepository(new InMemoryKingdomClockRepository()));
    }

    default DomainEventPublisher getDomainEventPublisher() {
        return DependencyLoaderAccess.findOptionalInstance(DomainEventPublisher.class)
                .orElseGet(() -> registerDomainEventPublisher(new RecordingDomainEventPublisher()));
    }

    default Clock getSystemClock() {
        return DependencyLoaderAccess.findOptionalInstance(Clock.class)
                .orElseGet(() -> registerSystemClock(Clock.systemUTC()));
    }

    default KingdomClockControlSystem getKingdomClockControlSystem() {
        return DependencyLoaderAccess.findOptionalInstance(KingdomClockControlSystem.class)
                .orElseGet(() -> registerKingdomClockControlSystem(new KingdomClockControlSystem()));
    }

    default KingdomClockPhaseCalculationHandler getKingdomClockPhaseCalculationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(KingdomClockPhaseCalculationHandler.class)
                .orElseGet(() -> registerKingdomClockPhaseCalculationHandler(new KingdomClockPhaseCalculationHandler()));
    }

    default KingdomScheduleWindowContainmentHandler getKingdomScheduleWindowContainmentHandler() {
        return DependencyLoaderAccess.findOptionalInstance(KingdomScheduleWindowContainmentHandler.class)
                .orElseGet(() -> registerKingdomScheduleWindowContainmentHandler(new KingdomScheduleWindowContainmentHandler()));
    }

    default KingdomClockRepository registerKingdomClockRepository(KingdomClockRepository repository) {
        DependencyLoaderAccess.registerInstance(KingdomClockRepository.class, repository);
        return repository;
    }

    default DomainEventPublisher registerDomainEventPublisher(DomainEventPublisher eventPublisher) {
        DependencyLoaderAccess.registerInstance(DomainEventPublisher.class, eventPublisher);
        return eventPublisher;
    }

    default Clock registerSystemClock(Clock systemClock) {
        DependencyLoaderAccess.registerInstance(Clock.class, systemClock);
        return systemClock;
    }

    default KingdomClockControlSystem registerKingdomClockControlSystem(KingdomClockControlSystem system) {
        DependencyLoaderAccess.registerInstance(KingdomClockControlSystem.class, system);
        return system;
    }

    default KingdomClockPhaseCalculationHandler registerKingdomClockPhaseCalculationHandler(KingdomClockPhaseCalculationHandler handler) {
        DependencyLoaderAccess.registerInstance(KingdomClockPhaseCalculationHandler.class, handler);
        return handler;
    }

    default KingdomScheduleWindowContainmentHandler registerKingdomScheduleWindowContainmentHandler(KingdomScheduleWindowContainmentHandler handler) {
        DependencyLoaderAccess.registerInstance(KingdomScheduleWindowContainmentHandler.class, handler);
        return handler;
    }
}
