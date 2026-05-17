package com.tavall.resourcegame.middleware.security;

public record TwoFactorEnrollmentCreated(
        TwoFactorEnrollment enrollment,
        String totpSecret
) {
}
