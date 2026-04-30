package com.tavall.hytale.resourcegame.middleware.security;

public interface ProtectedSecretCodec {
    String protect(String secret);

    String reveal(String protectedSecret);
}
