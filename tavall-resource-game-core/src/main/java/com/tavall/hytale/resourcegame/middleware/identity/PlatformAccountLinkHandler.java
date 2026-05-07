package com.tavall.hytale.resourcegame.middleware.identity;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.security.TokenHasher;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PlatformAccountLinkHandler {
    private final PlatformLinkChallengeRepository challengeRepository;
    private final PlatformAccountBindingRepository bindingRepository;
    private final TokenHasher tokenHasher;
    private final SecureRandom secureRandom;

    public PlatformAccountLinkHandler(
            PlatformLinkChallengeRepository challengeRepository,
            PlatformAccountBindingRepository bindingRepository,
            TokenHasher tokenHasher,
            SecureRandom secureRandom
    ) {
        this.challengeRepository = challengeRepository;
        this.bindingRepository = bindingRepository;
        this.tokenHasher = tokenHasher;
        this.secureRandom = secureRandom;
    }

    public PlatformLinkChallengeCreated createPlatformAccountLinkChallenge(
            UniversalPlayerId universalPlayerId,
            GamePlatform platform,
            Duration lifetime,
            Instant now
    ) {
        String shortCode = shortCode();
        PlatformLinkChallenge challenge = new PlatformLinkChallenge(
                UUID.randomUUID(),
                Optional.ofNullable(universalPlayerId),
                platform,
                Optional.empty(),
                tokenHasher.hashToken(shortCode),
                now.plus(lifetime),
                Optional.empty(),
                now,
                Map.of()
        );
        return new PlatformLinkChallengeCreated(challengeRepository.savePlatformLinkChallenge(challenge), shortCode);
    }

    public PlatformAccountBinding verifyPlatformAccountLink(
            UUID challengeId,
            String shortCode,
            GamePlatform platform,
            String platformAccountId,
            String platformDisplayName,
            Instant now
    ) {
        PlatformLinkChallenge challenge = validatedChallenge(challengeId, shortCode, platform, now);
        UniversalPlayerId universalPlayerId = challenge.universalPlayerId()
                .orElseThrow(() -> new IdentityOperationException("Platform link challenge is not attached to a universal account."));
        bindingRepository.findPlatformBinding(platform, platformAccountId)
                .filter(existing -> !existing.universalPlayerId().equals(universalPlayerId))
                .ifPresent(existing -> {
                    throw new IdentityOperationException("Platform account is already linked.");
                });
        PlatformAccountBinding binding = new PlatformAccountBinding(
                UUID.randomUUID(),
                universalPlayerId,
                platform,
                platformAccountId,
                platformDisplayName,
                true,
                now,
                now,
                Map.of("challengeId", challengeId.toString())
        );
        challengeRepository.savePlatformLinkChallenge(challenge.claimed(universalPlayerId, platformAccountId, now));
        return bindingRepository.savePlatformBinding(binding);
    }

    public PlatformAccountBinding relinkVerifiedPlatformAccount(
            UniversalPlayerId universalPlayerId,
            GamePlatform platform,
            String platformAccountId,
            String platformDisplayName,
            boolean highRiskChallengeVerified,
            Instant now
    ) {
        if (!highRiskChallengeVerified) {
            throw new IdentityOperationException("Platform relinking requires a high-risk action challenge.");
        }
        PlatformAccountBinding binding = new PlatformAccountBinding(
                UUID.randomUUID(),
                universalPlayerId,
                platform,
                platformAccountId,
                platformDisplayName,
                true,
                now,
                now,
                Map.of("relinked", "true")
        );
        return bindingRepository.savePlatformBinding(binding);
    }

    private PlatformLinkChallenge validatedChallenge(UUID challengeId, String shortCode, GamePlatform platform, Instant now) {
        PlatformLinkChallenge challenge = challengeRepository.findPlatformLinkChallenge(challengeId)
                .orElseThrow(() -> new IdentityOperationException("Platform link challenge was not found."));
        if (challenge.consumedAt().isPresent()) {
            throw new IdentityOperationException("Platform link challenge was already consumed.");
        }
        if (!challenge.expiresAt().isAfter(now)) {
            throw new IdentityOperationException("Platform link challenge expired.");
        }
        if (challenge.platform() != platform) {
            throw new IdentityOperationException("Platform link challenge platform mismatch.");
        }
        if (!challenge.shortCodeHash().equals(tokenHasher.hashToken(shortCode))) {
            throw new IdentityOperationException("Platform link challenge code is invalid.");
        }
        return challenge;
    }

    private String shortCode() {
        int value = secureRandom.nextInt(1_000_000);
        return String.format("%06d", value);
    }
}
