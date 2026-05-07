package com.tavall.hytale.resourcegame.middleware.security;

import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class TwoFactorEnrollmentHandler {
    private final TwoFactorRepository twoFactorRepository;
    private final ProtectedSecretCodec protectedSecretCodec;
    private final SecureRandom secureRandom;

    public TwoFactorEnrollmentHandler(
            TwoFactorRepository twoFactorRepository,
            ProtectedSecretCodec protectedSecretCodec,
            SecureRandom secureRandom
    ) {
        this.twoFactorRepository = twoFactorRepository;
        this.protectedSecretCodec = protectedSecretCodec;
        this.secureRandom = secureRandom;
    }

    public TwoFactorEnrollmentCreated createPendingTotpEnrollment(UniversalPlayerId universalPlayerId, Instant now) {
        byte[] secretBytes = new byte[20];
        secureRandom.nextBytes(secretBytes);
        String secret = Base64.getEncoder().encodeToString(secretBytes);
        TwoFactorEnrollment enrollment = new TwoFactorEnrollment(
                UUID.randomUUID(),
                universalPlayerId,
                TwoFactorMethod.TOTP,
                protectedSecretCodec.protect(secret),
                false,
                now,
                Optional.empty(),
                Optional.empty(),
                Map.of()
        );
        return new TwoFactorEnrollmentCreated(twoFactorRepository.saveEnrollment(enrollment), secret);
    }
}
