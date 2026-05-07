package com.tavall.hytale.resourcegame.middleware.authority;

import java.util.UUID;

public final class AuthorityGrantHandler {
    private final AuthorityRepository authorityRepository;
    private final ControlAuthorizationHandler authorizationHandler;

    public AuthorityGrantHandler(AuthorityRepository authorityRepository, ControlAuthorizationHandler authorizationHandler) {
        this.authorityRepository = authorityRepository;
        this.authorizationHandler = authorizationHandler;
    }

    public ControlAuthority grant(UUID grantedBy, AuthorityGrantRequest request, long nowEpochMillis) {
        authorizationHandler.requirePermission(grantedBy, com.tavall.hytale.resourcegame.middleware.control.ControlPermission.AUTHORITY_GRANT, ResourceTarget.global());
        ControlAuthority authority = new ControlAuthority(
                UUID.randomUUID(),
                request.grantedToPrincipalId(),
                request.authorityLevel(),
                request.scope(),
                request.permissions(),
                java.util.Set.of(),
                nowEpochMillis,
                request.expiresAtEpochMillis(),
                grantedBy,
                false,
                false,
                true
        );
        authorityRepository.saveAuthority(authority);
        return authority;
    }
}
