package org.tavall.control.asset;

import org.tavall.dependency.DependencyLoaderAccess;

public interface AssetDomain {
    default GlobalAssetRepository getGlobalAssetRepository() {
        return DependencyLoaderAccess.findInstance(GlobalAssetRepository.class);
    }

    default GlobalAssetResolutionHandler getGlobalAssetResolutionHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GlobalAssetResolutionHandler.class)
                .orElseGet(() -> registerGlobalAssetResolutionHandler(new GlobalAssetResolutionHandler()));
    }

    default PlatformAssetVersionRegistrationHandler getPlatformAssetVersionRegistrationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(PlatformAssetVersionRegistrationHandler.class)
                .orElseGet(() -> registerPlatformAssetVersionRegistrationHandler(new PlatformAssetVersionRegistrationHandler()));
    }

    default void registerGlobalAssetRepository(GlobalAssetRepository globalAssetRepository) {
        DependencyLoaderAccess.registerInstance(GlobalAssetRepository.class, globalAssetRepository);
    }

    default GlobalAssetResolutionHandler registerGlobalAssetResolutionHandler(GlobalAssetResolutionHandler globalAssetResolutionHandler) {
        DependencyLoaderAccess.registerInstance(GlobalAssetResolutionHandler.class, globalAssetResolutionHandler);
        return globalAssetResolutionHandler;
    }

    default PlatformAssetVersionRegistrationHandler registerPlatformAssetVersionRegistrationHandler(PlatformAssetVersionRegistrationHandler handler) {
        DependencyLoaderAccess.registerInstance(PlatformAssetVersionRegistrationHandler.class, handler);
        return handler;
    }
}
