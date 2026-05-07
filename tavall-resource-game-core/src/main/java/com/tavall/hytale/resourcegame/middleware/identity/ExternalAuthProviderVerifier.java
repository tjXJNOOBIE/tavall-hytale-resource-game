package com.tavall.hytale.resourcegame.middleware.identity;

public interface ExternalAuthProviderVerifier {
    VerifiedAuthIdentity verify(String providerToken);
}
