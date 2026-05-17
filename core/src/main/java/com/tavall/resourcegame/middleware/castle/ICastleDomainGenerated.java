package com.tavall.resourcegame.middleware.castle;

import com.tavall.resourcegame.dependency.DependencyLoaderAccess;

public interface ICastleDomainGenerated {
    default CastleRepository getCastleRepository() {
        return DependencyLoaderAccess.findOptionalInstance(CastleRepository.class)
                .orElseGet(() -> registerCastleRepository(new InMemoryCastleRepository()));
    }

    default CastleCreationHandler getCastleCreationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(CastleCreationHandler.class)
                .orElseGet(() -> registerCastleCreationHandler(new CastleCreationHandler()));
    }

    default CastlePlacementHandler getCastlePlacementHandler() {
        return DependencyLoaderAccess.findOptionalInstance(CastlePlacementHandler.class)
                .orElseGet(() -> registerCastlePlacementHandler(new CastlePlacementHandler()));
    }

    default CastleOwnershipValidationHandler getCastleOwnershipValidationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(CastleOwnershipValidationHandler.class)
                .orElseGet(() -> registerCastleOwnershipValidationHandler(new CastleOwnershipValidationHandler()));
    }

    default CastleRepository registerCastleRepository(CastleRepository castleRepository) {
        DependencyLoaderAccess.registerInstance(CastleRepository.class, castleRepository);
        return castleRepository;
    }

    default CastleCreationHandler registerCastleCreationHandler(CastleCreationHandler handler) {
        DependencyLoaderAccess.registerInstance(CastleCreationHandler.class, handler);
        return handler;
    }

    default CastlePlacementHandler registerCastlePlacementHandler(CastlePlacementHandler handler) {
        DependencyLoaderAccess.registerInstance(CastlePlacementHandler.class, handler);
        return handler;
    }

    default CastleOwnershipValidationHandler registerCastleOwnershipValidationHandler(CastleOwnershipValidationHandler handler) {
        DependencyLoaderAccess.registerInstance(CastleOwnershipValidationHandler.class, handler);
        return handler;
    }
}
