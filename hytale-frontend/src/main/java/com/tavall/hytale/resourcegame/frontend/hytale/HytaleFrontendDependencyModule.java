package com.tavall.hytale.resourcegame.frontend.hytale;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;

/**
 * Registers the single-surface Hytale adapter behind interface tokens so the Hytale plugin and
 * control-server tests can share the same command bridge without constructor-wired graphs.
 */
public final class HytaleFrontendDependencyModule {
    public void registerDependencies() {
        registerIfMissing(IHytaleFrontendConfig.class, HytaleFrontendConfig.fromEnvironment(System.getenv()));
        registerIfMissing(IHytaleFrontendModule.class, new HytaleFrontendModule());
        registerIfMissing(IHytaleFrontendCommandEnvelopeFactory.class, new HytaleFrontendCommandEnvelopeFactory());
        registerIfMissing(IHytaleKdCommandInputFormatterHandler.class, new HytaleKdCommandInputFormatterHandler());
        registerIfMissing(IHytaleKdCommandEnvelopeBridge.class, new HytaleKdCommandEnvelopeBridge());
        registerIfMissing(IHytaleControlCommandClient.class, new HytaleTcpControlCommandClient());
        registerIfMissing(IHytaleControlPlaneCommandBridge.class, new HytaleControlPlaneCommandBridge());
    }

    private <T> void registerIfMissing(Class<T> token, T instance) {
        if (!DependencyLoaderAccess.isInstanceRegistered(token)) {
            DependencyLoaderAccess.registerInstance(token, instance);
        }
    }
}
