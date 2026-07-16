package org.tavall.control.identity;

import java.util.Map;
import java.util.Optional;

public record VerifiedAuthIdentity(
        AuthProvider provider,
        String providerSubject,
        Optional<String> email,
        boolean emailVerified,
        Map<String, String> metadata
) {
    public VerifiedAuthIdentity {
        email = email == null ? Optional.empty() : email.map(String::toLowerCase);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
