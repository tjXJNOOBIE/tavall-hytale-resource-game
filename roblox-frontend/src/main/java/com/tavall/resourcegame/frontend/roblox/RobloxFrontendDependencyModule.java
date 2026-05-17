package com.tavall.resourcegame.frontend.roblox;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;

public final class RobloxFrontendDependencyModule {
    public void registerDependencies() {
        registerDependencies(new RobloxNoopControlCommandClient());
    }

    public void registerDependencies(IRobloxControlCommandClient controlCommandClient) {
        registerIfMissing(IRobloxFrontendModule.class, new RobloxFrontendModule());
        registerIfMissing(IRobloxFrontendCommandEnvelopeFactory.class, new RobloxFrontendCommandEnvelopeFactory());
        registerIfMissing(IRobloxKdCommandInputFormatterHandler.class, new RobloxKdCommandInputFormatterHandler());
        registerIfMissing(IRobloxKdCommandEnvelopeBridge.class, new RobloxKdCommandEnvelopeBridge());
        registerIfMissing(IRobloxControlCommandClient.class, controlCommandClient);
        registerIfMissing(IRobloxControlPlaneCommandBridge.class, new RobloxControlPlaneCommandBridge());
    }

    private <T> void registerIfMissing(Class<T> token, T instance) {
        if (!DependencyLoaderAccess.isInstanceRegistered(token)) {
            DependencyLoaderAccess.registerInstance(token, instance);
        }
    }
}
