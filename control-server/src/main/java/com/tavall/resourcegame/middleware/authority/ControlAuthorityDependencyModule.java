package com.tavall.resourcegame.middleware.authority;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.tjxjnoobie.api.dependency.IDependencyModule;
import com.tavall.resourcegame.middleware.control.ControlCommandRegistry;

public final class ControlAuthorityDependencyModule implements IDependencyModule {
    @Override
    public void registerDependencies() {
        DependencyLoaderAccess.findOptionalInstance(AuthorityRepository.class)
                .orElseGet(() -> register(AuthorityRepository.class, new InMemoryAuthorityRepository()));
        DependencyLoaderAccess.findOptionalInstance(PermissionPolicyRepository.class)
                .orElseGet(() -> register(PermissionPolicyRepository.class, new InMemoryPermissionPolicyRepository()));
        DependencyLoaderAccess.findOptionalInstance(AuthorizationAuditRepository.class)
                .orElseGet(() -> register(AuthorizationAuditRepository.class, new InMemoryAuthorizationAuditRepository()));
        DependencyLoaderAccess.findOptionalInstance(ControlCommandRegistry.class)
                .orElseGet(() -> register(ControlCommandRegistry.class, new ControlCommandRegistry()));
        DependencyLoaderAccess.registerInstance(IControlAuthorizationHandler.class, new ControlAuthorizationHandler());
        DependencyLoaderAccess.registerInstance(IAuthorityGrantHandler.class, new AuthorityGrantHandler());
    }

    private <T> T register(Class<T> token, T instance) {
        DependencyLoaderAccess.registerInstance(token, instance);
        return instance;
    }
}
