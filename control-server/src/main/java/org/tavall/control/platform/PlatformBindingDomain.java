package org.tavall.control.platform;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.control.identity.PlatformAccountLinkHandler;

public interface PlatformBindingDomain {
    default PlatformAccountLinkHandler getPlatformAccountLinkHandler() {
        return DependencyLoaderAccess.findInstance(PlatformAccountLinkHandler.class);
    }

    default void registerPlatformAccountLinkHandler(PlatformAccountLinkHandler platformAccountLinkHandler) {
        DependencyLoaderAccess.registerInstance(PlatformAccountLinkHandler.class, platformAccountLinkHandler);
    }
}
