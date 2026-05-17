package com.tavall.resourcegame.middleware.identity;

public interface ExternalAuthProviderVerifier {
    VerifiedAuthIdentity verify(String providerToken);
}
