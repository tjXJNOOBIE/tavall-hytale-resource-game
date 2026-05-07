package com.tavall.hytale.resourcegame.middleware.security;

public record TwoFactorEnrollmentCreated(
        TwoFactorEnrollment enrollment,
        String totpSecret
) {
}
