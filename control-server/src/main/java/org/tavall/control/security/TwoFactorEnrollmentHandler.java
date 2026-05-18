package org.tavall.control.security;

import org.tavall.control.identity.UniversalPlayerId;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class TwoFactorEnrollmentHandler implements ISecurityDomain {
    public TwoFactorEnrollmentHandler() {
    }

    public TwoFactorEnrollmentHandler(
            TwoFactorRepository twoFactorRepository,
            ProtectedSecretCodec protectedSecretCodec,
            SecureRandom secureRandom
    ) {
        registerTwoFactorRepository(twoFactorRepository);
        registerProtectedSecretCodec(protectedSecretCodec);
        registerSecureRandom(secureRandom);
    }

    public TwoFactorEnrollmentCreated createPendingTotpEnrollment(UniversalPlayerId universalPlayerId, Instant now) {
        byte[] secretBytes = new byte[20];
        getSecureRandom().nextBytes(secretBytes);
        String secret = Base64.getEncoder().encodeToString(secretBytes);
        TwoFactorEnrollment enrollment = new TwoFactorEnrollment(
                UUID.randomUUID(),
                universalPlayerId,
                TwoFactorMethod.TOTP,
                getProtectedSecretCodec().protect(secret),
                false,
                now,
                Optional.empty(),
                Optional.empty(),
                Map.of()
        );
        return new TwoFactorEnrollmentCreated(getTwoFactorRepository().saveEnrollment(enrollment), secret);
    }
}
