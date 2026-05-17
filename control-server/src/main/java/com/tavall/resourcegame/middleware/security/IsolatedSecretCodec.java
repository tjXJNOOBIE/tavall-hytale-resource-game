package com.tavall.resourcegame.middleware.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public final class IsolatedSecretCodec implements ProtectedSecretCodec {
    private static final String PREFIX = "protected:";

    @Override
    public String protect(String secret) {
        Objects.requireNonNull(secret, "secret");
        return PREFIX + Base64.getEncoder().encodeToString(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String reveal(String protectedSecret) {
        Objects.requireNonNull(protectedSecret, "protectedSecret");
        if (!protectedSecret.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Unsupported protected secret format.");
        }
        String encoded = protectedSecret.substring(PREFIX.length());
        return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
    }
}
