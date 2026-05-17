package com.tavall.resourcegame.middleware.authority;

import java.util.UUID;

public final class AuthorityGrantHandler implements IAuthorityGrantHandler, IControlAuthorityDomain {
    /**
     * Grants are routed through the same permission check as commands so delegation cannot bypass the authority kernel.
     */
    public ControlAuthority grant(UUID grantedBy, AuthorityGrantRequest request, long nowEpochMillis) {
        getControlAuthorizationHandler().requirePermission(grantedBy, com.tavall.resourcegame.middleware.control.ControlPermission.AUTHORITY_GRANT, ResourceTarget.global());
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
        getAuthorityRepository().saveAuthority(authority);
        return authority;
    }
}
