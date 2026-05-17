package com.tavall.resourcegame.frontend.roblox;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;

public interface IRobloxFrontendDomainGenerated {
    default IRobloxFrontendModule getRobloxFrontendModule() {
        return DependencyLoaderAccess.requireInstance(IRobloxFrontendModule.class);
    }

    default IRobloxFrontendCommandEnvelopeFactory getRobloxFrontendCommandEnvelopeFactory() {
        return DependencyLoaderAccess.requireInstance(IRobloxFrontendCommandEnvelopeFactory.class);
    }

    default IRobloxKdCommandInputFormatterHandler getRobloxKdCommandInputFormatterHandler() {
        return DependencyLoaderAccess.requireInstance(IRobloxKdCommandInputFormatterHandler.class);
    }

    default IRobloxKdCommandEnvelopeBridge getRobloxKdCommandEnvelopeBridge() {
        return DependencyLoaderAccess.requireInstance(IRobloxKdCommandEnvelopeBridge.class);
    }

    default IRobloxControlCommandClient getRobloxControlCommandClient() {
        return DependencyLoaderAccess.requireInstance(IRobloxControlCommandClient.class);
    }

    default IRobloxControlPlaneCommandBridge getRobloxControlPlaneCommandBridge() {
        return DependencyLoaderAccess.requireInstance(IRobloxControlPlaneCommandBridge.class);
    }
}
