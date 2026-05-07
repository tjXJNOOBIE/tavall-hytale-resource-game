package com.tavall.hytale.resourcegame.middleware.authority;

import com.tavall.hytale.resourcegame.dependency.DependencyLoaderAccess;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRegistry;

public interface IControlAuthorityDomainGenerated {
    default AuthorityRepository getAuthorityRepository() {
        return DependencyLoaderAccess.findInstance(AuthorityRepository.class);
    }

    default PermissionPolicyRepository getPermissionPolicyRepository() {
        return DependencyLoaderAccess.findInstance(PermissionPolicyRepository.class);
    }

    default AuthorizationAuditRepository getAuthorizationAuditRepository() {
        return DependencyLoaderAccess.findInstance(AuthorizationAuditRepository.class);
    }

    default ControlCommandRegistry getControlCommandRegistry() {
        return DependencyLoaderAccess.findInstance(ControlCommandRegistry.class);
    }

    default IControlAuthorizationHandler getControlAuthorizationHandler() {
        return DependencyLoaderAccess.findInstance(IControlAuthorizationHandler.class);
    }

    default IAuthorityGrantHandler getAuthorityGrantHandler() {
        return DependencyLoaderAccess.findInstance(IAuthorityGrantHandler.class);
    }
}
