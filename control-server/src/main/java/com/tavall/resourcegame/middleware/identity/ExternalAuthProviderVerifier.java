package org.tavall.control.identity;

public interface ExternalAuthProviderVerifier {
    VerifiedAuthIdentity verify(String providerToken);
}
