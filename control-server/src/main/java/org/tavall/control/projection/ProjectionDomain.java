package org.tavall.control.projection;

import org.tavall.dependency.DependencyLoaderAccess;
import org.tavall.control.asset.GlobalAssetResolutionHandler;
import org.tavall.control.guild.GuildActionValidationHandler;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendActionCatalog;

public interface ProjectionDomain {
    default GlobalAssetResolutionHandler getGlobalAssetResolutionHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GlobalAssetResolutionHandler.class)
                .orElseGet(() -> registerGlobalAssetResolutionHandler(new GlobalAssetResolutionHandler()));
    }

    default GlobalAssetProjectionHandler getGlobalAssetProjectionHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GlobalAssetProjectionHandler.class)
                .orElseGet(() -> registerGlobalAssetProjectionHandler(new GlobalAssetProjectionHandler()));
    }

    default FrontendProjectionHandler getFrontendProjectionHandler() {
        return DependencyLoaderAccess.findOptionalInstance(FrontendProjectionHandler.class)
                .orElseGet(() -> registerFrontendProjectionHandler(new FrontendProjectionHandler()));
    }

    default ResourceGameFrontendActionCatalog getResourceGameFrontendActionCatalog() {
        return DependencyLoaderAccess.findOptionalInstance(ResourceGameFrontendActionCatalog.class)
                .orElseGet(() -> registerResourceGameFrontendActionCatalog(new ResourceGameFrontendActionCatalog()));
    }

    default GuildActionValidationHandler getGuildActionValidationHandler() {
        return DependencyLoaderAccess.findInstance(GuildActionValidationHandler.class);
    }

    default GlobalAssetProjectionHandler registerGlobalAssetProjectionHandler(GlobalAssetProjectionHandler globalAssetProjectionHandler) {
        DependencyLoaderAccess.registerInstance(GlobalAssetProjectionHandler.class, globalAssetProjectionHandler);
        return globalAssetProjectionHandler;
    }

    default FrontendProjectionHandler registerFrontendProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        DependencyLoaderAccess.registerInstance(FrontendProjectionHandler.class, frontendProjectionHandler);
        return frontendProjectionHandler;
    }

    default ResourceGameFrontendActionCatalog registerResourceGameFrontendActionCatalog(ResourceGameFrontendActionCatalog actionCatalog) {
        DependencyLoaderAccess.registerInstance(ResourceGameFrontendActionCatalog.class, actionCatalog);
        return actionCatalog;
    }

    default void registerGuildActionValidationHandler(GuildActionValidationHandler guildActionValidationHandler) {
        DependencyLoaderAccess.registerInstance(GuildActionValidationHandler.class, guildActionValidationHandler);
    }

    default GlobalAssetResolutionHandler registerGlobalAssetResolutionHandler(GlobalAssetResolutionHandler globalAssetResolutionHandler) {
        DependencyLoaderAccess.registerInstance(GlobalAssetResolutionHandler.class, globalAssetResolutionHandler);
        return globalAssetResolutionHandler;
    }
}
