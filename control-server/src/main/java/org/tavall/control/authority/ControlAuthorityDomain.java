package org.tavall.control.authority;

import org.tavall.dependency.DependencyLoaderAccess;
import org.tavall.control.runtime.ControlCommandRegistry;

public interface ControlAuthorityDomain {
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
