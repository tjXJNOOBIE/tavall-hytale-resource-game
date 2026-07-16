package org.tavall.control.security;

public record TwoFactorEnrollmentCreated(
        TwoFactorEnrollment enrollment,
        String totpSecret
) {
}
