package com.tavall.resourcegame.middleware.identity;

import com.tavall.resourcegame.middleware.common.GamePlatform;
import com.tavall.resourcegame.middleware.security.Sha256TokenHasher;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class IdentityAuthIntegrationTest {
    @Test
    void passwordlessEmailStoresHashedOneTimeChallengeAndCreatesUniversalAccount() {
        InMemoryIdentityRepository repository = new InMemoryIdentityRepository();
        Sha256TokenHasher tokenHasher = new Sha256TokenHasher("test-pepper");
        PasswordlessEmailAuthHandler authHandler = new PasswordlessEmailAuthHandler(
                repository,
                repository,
                repository,
                tokenHasher,
                new SecureRandom(new byte[]{1, 2, 3})
        );
        Instant now = Instant.parse("2026-04-30T12:00:00Z");

        PasswordlessEmailChallengeCreated created = authHandler.requestSignInCode("PLAYER@Example.com", Duration.ofMinutes(10), now);

        assertNotEquals(created.deliveryToken(), created.challenge().challengeTokenHash());
        assertEquals("player@example.com", created.challenge().email());
        assertThrows(IdentityOperationException.class, () -> authHandler.verifySignInCode(created.challenge().challengeId(), "wrong-token", now.plusSeconds(1)));

        UniversalPlayerAccount account = authHandler.verifySignInCode(created.challenge().challengeId(), created.deliveryToken(), now.plusSeconds(2));

        assertTrue(repository.findAccount(account.universalPlayerId()).isPresent());
        assertTrue(repository.findAuthIdentity(AuthProvider.PASSWORDLESS_EMAIL, "player@example.com").isPresent());
        assertTrue(repository.findPasswordlessChallenge(created.challenge().challengeId()).orElseThrow().consumedAt().isPresent());
        assertThrows(IdentityOperationException.class, () -> authHandler.verifySignInCode(created.challenge().challengeId(), created.deliveryToken(), now.plusSeconds(3)));
    }

    @Test
    void providerIdentityAndPlatformBindingsUseUniversalPlayerId() {
        InMemoryIdentityRepository repository = new InMemoryIdentityRepository();
        UniversalPlayerAccount account = new UniversalPlayerAccountHandler(repository)
                .createUniversalAccount("Cross Player", Optional.of("cross@example.com"), Instant.parse("2026-04-30T12:05:00Z"));
        ProviderAuthLinkHandler providerAuthLinkHandler = new ProviderAuthLinkHandler(repository);
        Instant now = Instant.parse("2026-04-30T12:06:00Z");

        AuthIdentity googleIdentity = providerAuthLinkHandler.linkVerifiedProviderIdentity(
                account.universalPlayerId(),
                new VerifiedAuthIdentity(AuthProvider.GOOGLE, "google-subject-1", Optional.of("cross@example.com"), true, Map.of("issuer", "google")),
                now
        );

        assertEquals(account.universalPlayerId(), googleIdentity.universalPlayerId());

        PlatformAccountLinkHandler platformAccountLinkHandler = new PlatformAccountLinkHandler(
                repository,
                repository,
                new Sha256TokenHasher("link-pepper"),
                new SecureRandom(new byte[]{4, 5, 6})
        );
        PlatformLinkChallengeCreated challenge = platformAccountLinkHandler.createPlatformAccountLinkChallenge(
                account.universalPlayerId(),
                GamePlatform.ROBLOX,
                Duration.ofMinutes(5),
                now
        );

        PlatformAccountBinding binding = platformAccountLinkHandler.verifyPlatformAccountLink(
                challenge.challenge().challengeId(),
                challenge.shortCode(),
                GamePlatform.ROBLOX,
                "123456789",
                "RobloxPlayer",
                now.plusSeconds(1)
        );

        assertEquals(account.universalPlayerId(), binding.universalPlayerId());
        assertEquals(GamePlatform.ROBLOX, binding.platform());
        assertFalse(repository.findPlatformBindings(account.universalPlayerId()).isEmpty());

        UniversalPlayerAccount secondAccount = new UniversalPlayerAccountHandler(repository)
                .createUniversalAccount("Other", Optional.empty(), now.plusSeconds(2));
        PlatformLinkChallengeCreated secondChallenge = platformAccountLinkHandler.createPlatformAccountLinkChallenge(
                secondAccount.universalPlayerId(),
                GamePlatform.ROBLOX,
                Duration.ofMinutes(5),
                now.plusSeconds(3)
        );
        assertThrows(IdentityOperationException.class, () -> platformAccountLinkHandler.verifyPlatformAccountLink(
                secondChallenge.challenge().challengeId(),
                secondChallenge.shortCode(),
                GamePlatform.ROBLOX,
                "123456789",
                "Duplicate",
                now.plusSeconds(4)
        ));
    }

    @Test
    void discordPlatformBindingRejectsDuplicateDiscordUserId() {
        InMemoryIdentityRepository repository = new InMemoryIdentityRepository();
        PlatformAccountLinkHandler platformAccountLinkHandler = new PlatformAccountLinkHandler(
                repository,
                repository,
                new Sha256TokenHasher("discord-link-pepper"),
                new SecureRandom(new byte[]{7, 7, 7})
        );
        Instant now = Instant.parse("2026-04-30T12:08:00Z");
        UniversalPlayerAccount firstAccount = new UniversalPlayerAccountHandler(repository).createUniversalAccount("Discord One", Optional.empty(), now);
        UniversalPlayerAccount secondAccount = new UniversalPlayerAccountHandler(repository).createUniversalAccount("Discord Two", Optional.empty(), now);

        PlatformLinkChallengeCreated firstChallenge = platformAccountLinkHandler.createPlatformAccountLinkChallenge(firstAccount.universalPlayerId(), GamePlatform.DISCORD, Duration.ofMinutes(5), now);
        platformAccountLinkHandler.verifyPlatformAccountLink(firstChallenge.challenge().challengeId(), firstChallenge.shortCode(), GamePlatform.DISCORD, "discord-user-42", "DiscordOne", now.plusSeconds(1));

        PlatformLinkChallengeCreated secondChallenge = platformAccountLinkHandler.createPlatformAccountLinkChallenge(secondAccount.universalPlayerId(), GamePlatform.DISCORD, Duration.ofMinutes(5), now.plusSeconds(2));
        assertThrows(IdentityOperationException.class, () -> platformAccountLinkHandler.verifyPlatformAccountLink(secondChallenge.challenge().challengeId(), secondChallenge.shortCode(), GamePlatform.DISCORD, "discord-user-42", "DiscordTwo", now.plusSeconds(3)));
    }
}
