package org.tavall.control.security;

public interface ProtectedSecretCodec {
    String protect(String secret);

    String reveal(String protectedSecret);
}
