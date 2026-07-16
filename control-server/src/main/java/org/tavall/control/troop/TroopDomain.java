package org.tavall.control.troop;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;

public interface TroopDomain {
    default TroopRepository getTroopRepository() {
        return DependencyLoaderAccess.findOptionalInstance(TroopRepository.class)
                .orElseGet(() -> registerTroopRepository(new InMemoryTroopRepository()));
    }

    default TroopRegistrationHandler getTroopRegistrationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(TroopRegistrationHandler.class)
                .orElseGet(() -> registerTroopRegistrationHandler(new TroopRegistrationHandler()));
    }

    default TroopMovementHandler getTroopMovementHandler() {
        return DependencyLoaderAccess.findOptionalInstance(TroopMovementHandler.class)
                .orElseGet(() -> registerTroopMovementHandler(new TroopMovementHandler()));
    }

    default TroopStatusTransitionHandler getTroopStatusTransitionHandler() {
        return DependencyLoaderAccess.findOptionalInstance(TroopStatusTransitionHandler.class)
                .orElseGet(() -> registerTroopStatusTransitionHandler(new TroopStatusTransitionHandler()));
    }

    default WoundedTroopCaptureHandler getWoundedTroopCaptureHandler() {
        return DependencyLoaderAccess.findOptionalInstance(WoundedTroopCaptureHandler.class)
                .orElseGet(() -> registerWoundedTroopCaptureHandler(new WoundedTroopCaptureHandler()));
    }

    default GuildWarDeclarationHandler getGuildWarDeclarationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildWarDeclarationHandler.class)
                .orElseGet(() -> registerGuildWarDeclarationHandler(new GuildWarDeclarationHandler()));
    }

    default TroopRepository registerTroopRepository(TroopRepository troopRepository) {
        DependencyLoaderAccess.registerInstance(TroopRepository.class, troopRepository);
        return troopRepository;
    }

    default TroopRegistrationHandler registerTroopRegistrationHandler(TroopRegistrationHandler handler) {
        DependencyLoaderAccess.registerInstance(TroopRegistrationHandler.class, handler);
        return handler;
    }

    default TroopMovementHandler registerTroopMovementHandler(TroopMovementHandler handler) {
        DependencyLoaderAccess.registerInstance(TroopMovementHandler.class, handler);
        return handler;
    }

    default TroopStatusTransitionHandler registerTroopStatusTransitionHandler(TroopStatusTransitionHandler handler) {
        DependencyLoaderAccess.registerInstance(TroopStatusTransitionHandler.class, handler);
        return handler;
    }

    default WoundedTroopCaptureHandler registerWoundedTroopCaptureHandler(WoundedTroopCaptureHandler handler) {
        DependencyLoaderAccess.registerInstance(WoundedTroopCaptureHandler.class, handler);
        return handler;
    }

    default GuildWarDeclarationHandler registerGuildWarDeclarationHandler(GuildWarDeclarationHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildWarDeclarationHandler.class, handler);
        return handler;
    }
}
