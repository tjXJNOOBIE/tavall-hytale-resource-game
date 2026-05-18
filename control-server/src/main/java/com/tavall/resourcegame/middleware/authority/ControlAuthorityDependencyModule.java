package org.tavall.control.authority;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.tjxjnoobie.api.dependency.IDependencyModule;
import org.tavall.control.runtime.ControlCommandRegistry;

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
