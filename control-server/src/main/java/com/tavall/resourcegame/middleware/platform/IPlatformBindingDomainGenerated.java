package com.tavall.resourcegame.middleware.platform;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.middleware.identity.PlatformAccountLinkHandler;

public interface IPlatformBindingDomainGenerated {
    default PlatformAccountLinkHandler getPlatformAccountLinkHandler() {
        return DependencyLoaderAccess.findInstance(PlatformAccountLinkHandler.class);
    }

    default void registerPlatformAccountLinkHandler(PlatformAccountLinkHandler platformAccountLinkHandler) {
        DependencyLoaderAccess.registerInstance(PlatformAccountLinkHandler.class, platformAccountLinkHandler);
    }
}
