package com.tavall.hytale.resourcegame.middleware.identity;

import java.time.Instant;
import java.util.UUID;

public final class ProviderAuthLinkHandler {
    private final AuthIdentityRepository authIdentityRepository;

    public ProviderAuthLinkHandler(AuthIdentityRepository authIdentityRepository) {
        this.authIdentityRepository = authIdentityRepository;
    }

    public AuthIdentity linkVerifiedProviderIdentity(UniversalPlayerId universalPlayerId, VerifiedAuthIdentity verifiedIdentity, Instant now) {
        authIdentityRepository.findAuthIdentity(verifiedIdentity.provider(), verifiedIdentity.providerSubject())
                .filter(existing -> !existing.universalPlayerId().equals(universalPlayerId))
                .ifPresent(existing -> {
                    throw new IdentityOperationException("Provider identity is already linked.");
                });
        return authIdentityRepository.saveAuthIdentity(new AuthIdentity(
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
