package com.tavall.resourcegame.middleware.security;

public interface TokenHasher {
    String hashToken(String token);
}
