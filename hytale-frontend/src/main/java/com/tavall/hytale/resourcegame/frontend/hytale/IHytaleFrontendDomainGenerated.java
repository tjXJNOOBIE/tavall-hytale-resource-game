package com.tavall.hytale.resourcegame.frontend.hytale;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;

public interface IHytaleFrontendDomainGenerated {
    default IHytaleFrontendConfig getHytaleFrontendConfig() {
        return DependencyLoaderAccess.requireInstance(IHytaleFrontendConfig.class);
    }

    default IHytaleFrontendCommandEnvelopeFactory getHytaleFrontendCommandEnvelopeFactory() {
        return DependencyLoaderAccess.requireInstance(IHytaleFrontendCommandEnvelopeFactory.class);
    }

    default IHytaleKdCommandInputFormatterHandler getHytaleKdCommandInputFormatterHandler() {
        return DependencyLoaderAccess.requireInstance(IHytaleKdCommandInputFormatterHandler.class);
    }

    default IHytaleKdCommandEnvelopeBridge getHytaleKdCommandEnvelopeBridge() {
        return DependencyLoaderAccess.requireInstance(IHytaleKdCommandEnvelopeBridge.class);
    }

    default IHytaleControlCommandClient getHytaleControlCommandClient() {
        return DependencyLoaderAccess.requireInstance(IHytaleControlCommandClient.class);
    }

    default IHytaleControlPlaneCommandBridge getHytaleControlPlaneCommandBridge() {
        return DependencyLoaderAccess.requireInstance(IHytaleControlPlaneCommandBridge.class);
    }

    default IHytaleFrontendModule getHytaleFrontendModule() {
        return DependencyLoaderAccess.requireInstance(IHytaleFrontendModule.class);
    }
}
