package com.tavall.resourcegame.middleware.identity;

import com.tavall.resourcegame.middleware.security.ISecurityDomain;
import com.tavall.resourcegame.middleware.security.TokenHasher;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PasswordlessEmailAuthHandler implements IIdentityDomain, ISecurityDomain {
    private static final int MAX_ATTEMPTS = 5;

    public PasswordlessEmailAuthHandler() {
    }

    public PasswordlessEmailAuthHandler(
            PasswordlessEmailChallengeRepository challengeRepository,
            AuthIdentityRepository authIdentityRepository,
            UniversalPlayerAccountRepository accountRepository,
            TokenHasher tokenHasher,
            SecureRandom secureRandom
    ) {
        registerPasswordlessEmailChallengeRepository(challengeRepository);
        registerAuthIdentityRepository(authIdentityRepository);
        registerUniversalPlayerAccountRepository(accountRepository);
        registerTokenHasher(tokenHasher);
        registerSecureRandom(secureRandom);
    }

    public PasswordlessEmailChallengeCreated requestSignInCode(String email, Duration lifetime, Instant now) {
        String deliveryToken = newToken();
        PasswordlessEmailChallenge challenge = new PasswordlessEmailChallenge(
                UUID.randomUUID(),
                email,
                getTokenHasher().hashToken(deliveryToken),
                now.plus(lifetime),
                Optional.empty(),
                0,
                Map.of("delivery", "TODO_EMAIL_PROVIDER")
        );
        return new PasswordlessEmailChallengeCreated(getPasswordlessEmailChallengeRepository().savePasswordlessChallenge(challenge), deliveryToken);
    }

    public UniversalPlayerAccount verifySignInCode(UUID challengeId, String token, Instant now) {
        PasswordlessEmailChallenge challenge = getPasswordlessEmailChallengeRepository().findPasswordlessChallenge(challengeId)
                .orElseThrow(() -> new IdentityOperationException("Passwordless challenge was not found."));
        if (challenge.consumedAt().isPresent()) {
            throw new IdentityOperationException("Passwordless challenge was already consumed.");
        }
        if (!challenge.expiresAt().isAfter(now)) {
            throw new IdentityOperationException("Passwordless challenge expired.");
        }
        if (challenge.attemptCount() >= MAX_ATTEMPTS) {
            throw new IdentityOperationException("Passwordless challenge attempt limit reached.");
        }
        if (!challenge.challengeTokenHash().equals(getTokenHasher().hashToken(token))) {
            getPasswordlessEmailChallengeRepository().savePasswordlessChallenge(challenge.withAttemptCount(challenge.attemptCount() + 1));
            throw new IdentityOperationException("Passwordless challenge token is invalid.");
        }

        Optional<AuthIdentity> existingIdentity = getAuthIdentityRepository().findAuthIdentity(AuthProvider.PASSWORDLESS_EMAIL, challenge.email());
        UniversalPlayerAccount account = existingIdentity
                .flatMap(identity -> getUniversalPlayerAccountRepository().findAccount(identity.universalPlayerId()))
                .orElseGet(() -> getUniversalPlayerAccountRepository().saveAccount(new UniversalPlayerAccount(
                        UniversalPlayerId.random(),
                        challenge.email(),
                        Optional.of(challenge.email()),
                        now,
                        now,
                        false,
                        Map.of()
                )));
        getAuthIdentityRepository().saveAuthIdentity(new AuthIdentity(
                UUID.randomUUID(),
                account.universalPlayerId(),
                AuthProvider.PASSWORDLESS_EMAIL,
                challenge.email(),
                Optional.of(challenge.email()),
                true,
                now,
                now,
                Map.of()
        ));
        getPasswordlessEmailChallengeRepository().savePasswordlessChallenge(challenge.consumed(now));
        return account;
    }

    private String newToken() {
        byte[] bytes = new byte[18];
        getSecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
