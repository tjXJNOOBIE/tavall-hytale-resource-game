package com.tavall.hytale.resourcegame.middleware.security;

public interface TokenHasher {
    String hashToken(String token);
}
