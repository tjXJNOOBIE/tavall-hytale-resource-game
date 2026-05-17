package com.tavall.resourcegame.middleware.identity;

import java.time.Instant;
import java.util.UUID;

public final class ProviderAuthLinkHandler implements IIdentityDomain {
    public ProviderAuthLinkHandler() {
    }

    public ProviderAuthLinkHandler(AuthIdentityRepository authIdentityRepository) {
        registerAuthIdentityRepository(authIdentityRepository);
    }

    public AuthIdentity linkVerifiedProviderIdentity(UniversalPlayerId universalPlayerId, VerifiedAuthIdentity verifiedIdentity, Instant now) {
        getAuthIdentityRepository().findAuthIdentity(verifiedIdentity.provider(), verifiedIdentity.providerSubject())
                .filter(existing -> !existing.universalPlayerId().equals(universalPlayerId))
                .ifPresent(existing -> {
                    throw new IdentityOperationException("Provider identity is already linked.");
                });
        return getAuthIdentityRepository().saveAuthIdentity(new AuthIdentity(
                UUID.randomUUID(),
                universalPlayerId,
                verifiedIdentity.provider(),
                verifiedIdentity.providerSubject(),
                verifiedIdentity.email(),
                verifiedIdentity.emailVerified(),
                now,
                now,
                verifiedIdentity.metadata()
        ));
    }
}
