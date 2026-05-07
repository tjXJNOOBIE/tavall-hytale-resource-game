package com.tavall.hytale.resourcegame.middleware.identity;

import com.tavall.hytale.resourcegame.middleware.security.TokenHasher;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PasswordlessEmailAuthHandler {
    private static final int MAX_ATTEMPTS = 5;

    private final PasswordlessEmailChallengeRepository challengeRepository;
    private final AuthIdentityRepository authIdentityRepository;
    private final UniversalPlayerAccountRepository accountRepository;
    private final TokenHasher tokenHasher;
    private final SecureRandom secureRandom;

    public PasswordlessEmailAuthHandler(
            PasswordlessEmailChallengeRepository challengeRepository,
            AuthIdentityRepository authIdentityRepository,
            UniversalPlayerAccountRepository accountRepository,
            TokenHasher tokenHasher,
            SecureRandom secureRandom
    ) {
        this.challengeRepository = challengeRepository;
        this.authIdentityRepository = authIdentityRepository;
        this.accountRepository = accountRepository;
        this.tokenHasher = tokenHasher;
        this.secureRandom = secureRandom;
    }

    public PasswordlessEmailChallengeCreated requestSignInCode(String email, Duration lifetime, Instant now) {
        String deliveryToken = newToken();
        PasswordlessEmailChallenge challenge = new PasswordlessEmailChallenge(
                UUID.randomUUID(),
                email,
                tokenHasher.hashToken(deliveryToken),
                now.plus(lifetime),
                Optional.empty(),
                0,
                Map.of("delivery", "TODO_EMAIL_PROVIDER")
        );
        return new PasswordlessEmailChallengeCreated(challengeRepository.savePasswordlessChallenge(challenge), deliveryToken);
    }

    public UniversalPlayerAccount verifySignInCode(UUID challengeId, String token, Instant now) {
        PasswordlessEmailChallenge challenge = challengeRepository.findPasswordlessChallenge(challengeId)
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
        if (!challenge.challengeTokenHash().equals(tokenHasher.hashToken(token))) {
            challengeRepository.savePasswordlessChallenge(challenge.withAttemptCount(challenge.attemptCount() + 1));
            throw new IdentityOperationException("Passwordless challenge token is invalid.");
        }

        Optional<AuthIdentity> existingIdentity = authIdentityRepository.findAuthIdentity(AuthProvider.PASSWORDLESS_EMAIL, challenge.email());
        UniversalPlayerAccount account = existingIdentity
                .flatMap(identity -> accountRepository.findAccount(identity.universalPlayerId()))
                .orElseGet(() -> accountRepository.saveAccount(new UniversalPlayerAccount(
                        UniversalPlayerId.random(),
                        challenge.email(),
                        Optional.of(challenge.email()),
                        now,
                        now,
                        false,
                        Map.of()
                )));
        authIdentityRepository.saveAuthIdentity(new AuthIdentity(
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
        challengeRepository.savePasswordlessChallenge(challenge.consumed(now));
        return account;
    }

    private String newToken() {
        byte[] bytes = new byte[18];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
