package org.tavall.control.player;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.control.player.IIpHashHandler;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hashes IP addresses for safe storage.
 */
public final class IpHashHandler implements IIpHashHandler, IDependencyInjectableConcrete {
    public String hash(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return "unknown";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawValue.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte value : hashed) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            return "unknown";
        }
    }
}


