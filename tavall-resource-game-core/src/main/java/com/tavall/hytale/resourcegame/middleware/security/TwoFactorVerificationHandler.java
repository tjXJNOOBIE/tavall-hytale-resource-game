package com.tavall.hytale.resourcegame.middleware.security;

import java.time.Instant;
import java.util.UUID;

public final class TwoFactorVerificationHandler {
    private final TwoFactorRepository twoFactorRepository;
    private final ProtectedSecretCodec protectedSecretCodec;
    private final TotpCodeGenerator totpCodeGenerator;

    public TwoFactorVerificationHandler(
            TwoFactorRepository twoFactorRepository,
            ProtectedSecretCodec protectedSecretCodec,
            TotpCodeGenerator totpCodeGenerator
    ) {
        this.twoFactorRepository = twoFactorRepository;
        this.protectedSecretCodec = protectedSecretCodec;
        this.totpCodeGenerator = totpCodeGenerator;
    }

    public TwoFactorEnrollment verifyEnrollment(UUID enrollmentId, String totpCode, Instant now) {
        TwoFactorEnrollment enrollment = twoFactorRepository.findEnrollment(enrollmentId)
                .orElseThrow(() -> new SecurityException("Two-factor enrollment was not found."));
        if (enrollment.enabled()) {
            return enrollment;
        }
        String secret = protectedSecretCodec.reveal(enrollment.secretEncryptedOrProtected());
        if (!totpCodeGenerator.verifyCode(secret, totpCode, now)) {
            throw new SecurityException("Two-factor code is invalid.");
        }
        return twoFactorRepository.saveEnrollment(enrollment.enabled(now));
    }

    public boolean verifyEnabledTotp(TwoFactorEnrollment enrollment, String totpCode, Instant now) {
        String secret = protectedSecretCodec.reveal(enrollment.secretEncryptedOrProtected());
        boolean verified = totpCodeGenerator.verifyCode(secret, totpCode, now);
        if (verified) {
            twoFactorRepository.saveEnrollment(enrollment.used(now));
        }
        return verified;
    }
}
