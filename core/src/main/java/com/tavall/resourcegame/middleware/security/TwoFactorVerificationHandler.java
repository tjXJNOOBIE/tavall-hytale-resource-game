package com.tavall.resourcegame.middleware.security;

import java.time.Instant;
import java.util.UUID;

public final class TwoFactorVerificationHandler implements ISecurityDomain {
    public TwoFactorVerificationHandler() {
    }

    public TwoFactorVerificationHandler(
            TwoFactorRepository twoFactorRepository,
            ProtectedSecretCodec protectedSecretCodec,
            TotpCodeGenerator totpCodeGenerator
    ) {
        registerTwoFactorRepository(twoFactorRepository);
        registerProtectedSecretCodec(protectedSecretCodec);
        registerTotpCodeGenerator(totpCodeGenerator);
    }

    public TwoFactorEnrollment verifyEnrollment(UUID enrollmentId, String totpCode, Instant now) {
        TwoFactorEnrollment enrollment = getTwoFactorRepository().findEnrollment(enrollmentId)
                .orElseThrow(() -> new SecurityException("Two-factor enrollment was not found."));
        if (enrollment.enabled()) {
            return enrollment;
        }
        String secret = getProtectedSecretCodec().reveal(enrollment.secretEncryptedOrProtected());
        if (!getTotpCodeGenerator().verifyCode(secret, totpCode, now)) {
            throw new SecurityException("Two-factor code is invalid.");
        }
        return getTwoFactorRepository().saveEnrollment(enrollment.enabled(now));
    }

    public boolean verifyEnabledTotp(TwoFactorEnrollment enrollment, String totpCode, Instant now) {
        String secret = getProtectedSecretCodec().reveal(enrollment.secretEncryptedOrProtected());
        boolean verified = getTotpCodeGenerator().verifyCode(secret, totpCode, now);
        if (verified) {
            getTwoFactorRepository().saveEnrollment(enrollment.used(now));
        }
        return verified;
    }
}
