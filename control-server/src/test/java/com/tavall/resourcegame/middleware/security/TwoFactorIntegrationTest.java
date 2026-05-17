package com.tavall.resourcegame.middleware.security;

import com.tavall.resourcegame.middleware.common.HighRiskAction;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class TwoFactorIntegrationTest {
    @Test
    void totpEnrollmentMustBeVerifiedBeforeHighRiskActionsRequireChallenge() {
        InMemoryTwoFactorRepository repository = new InMemoryTwoFactorRepository();
        IsolatedSecretCodec secretCodec = new IsolatedSecretCodec();
        TotpCodeGenerator totpCodeGenerator = new TotpCodeGenerator();
        TwoFactorEnrollmentHandler enrollmentHandler = new TwoFactorEnrollmentHandler(repository, secretCodec, new SecureRandom(new byte[]{7, 8, 9}));
        TwoFactorVerificationHandler verificationHandler = new TwoFactorVerificationHandler(repository, secretCodec, totpCodeGenerator);
        HighRiskActionChallengeHandler highRiskActionChallengeHandler = new HighRiskActionChallengeHandler(repository);
        UniversalPlayerId playerId = UniversalPlayerId.random();
        Instant now = Instant.parse("2026-04-30T12:10:00Z");

        TwoFactorEnrollmentCreated created = enrollmentHandler.createPendingTotpEnrollment(playerId, now);

        assertFalse(highRiskActionChallengeHandler.requiresTwoFactorChallenge(playerId, HighRiskAction.PLATFORM_RELINK));
        assertThrows(SecurityException.class, () -> verificationHandler.verifyEnrollment(created.enrollment().enrollmentId(), "000000", now));

        String validCode = totpCodeGenerator.generateCode(created.totpSecret(), now);
        verificationHandler.verifyEnrollment(created.enrollment().enrollmentId(), validCode, now);

        assertTrue(highRiskActionChallengeHandler.requiresTwoFactorChallenge(playerId, HighRiskAction.PLATFORM_RELINK));
        assertThrows(SecurityException.class, () -> highRiskActionChallengeHandler.validateHighRiskAction(playerId, HighRiskAction.PLATFORM_RELINK, false));
        highRiskActionChallengeHandler.validateHighRiskAction(playerId, HighRiskAction.PLATFORM_RELINK, true);
    }

    @Test
    void backupCodesAreHashedAndSingleUse() {
        InMemoryTwoFactorRepository repository = new InMemoryTwoFactorRepository();
        TwoFactorRecoveryCodeHandler recoveryCodeHandler = new TwoFactorRecoveryCodeHandler(
                repository,
                new Sha256TokenHasher("recovery-pepper"),
                new SecureRandom(new byte[]{10, 11, 12})
        );
        UniversalPlayerId playerId = UniversalPlayerId.random();

        GeneratedRecoveryCodes generatedRecoveryCodes = recoveryCodeHandler.generateRecoveryCodes(playerId, 2, Instant.parse("2026-04-30T12:12:00Z"));

        String displayCode = generatedRecoveryCodes.displayCodes().getFirst();
        assertFalse(generatedRecoveryCodes.storedRecoveryCodes().getFirst().codeHash().equals(displayCode));
        assertTrue(recoveryCodeHandler.consumeRecoveryCode(playerId, displayCode, Instant.parse("2026-04-30T12:13:00Z")));
        assertFalse(recoveryCodeHandler.consumeRecoveryCode(playerId, displayCode, Instant.parse("2026-04-30T12:14:00Z")));
    }
}
