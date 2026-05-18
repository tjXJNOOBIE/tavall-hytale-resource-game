package org.tavall.control.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

public final class Sha256TokenHasher implements TokenHasher {
    private final String pepper;

    public Sha256TokenHasher(String pepper) {
        this.pepper = pepper == null ? "" : pepper;
    }

    @Override
    public String hashToken(String token) {
        Objects.requireNonNull(token, "token");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((pepper + ":" + token).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
