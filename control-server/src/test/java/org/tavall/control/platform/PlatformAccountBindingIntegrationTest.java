package org.tavall.control.platform;

import org.tavall.control.common.GamePlatform;
import org.tavall.control.identity.InMemoryIdentityRepository;
import org.tavall.control.identity.PlatformAccountBinding;
import org.tavall.control.identity.PlatformAccountLinkHandler;
import org.tavall.control.identity.PlatformLinkChallengeCreated;
import org.tavall.control.identity.UniversalPlayerId;
import org.tavall.control.security.Sha256TokenHasher;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class PlatformAccountBindingIntegrationTest {
    @Test
    void platformSpecificBindingHandlersUseCanonicalLinkFlow() {
        InMemoryIdentityRepository repository = new InMemoryIdentityRepository();
        PlatformAccountLinkHandler linkHandler = new PlatformAccountLinkHandler(repository, repository, new Sha256TokenHasher("platform-pepper"), new SecureRandom(new byte[]{2, 2, 2}));
        UniversalPlayerId playerId = UniversalPlayerId.random();
        Instant now = Instant.parse("2026-04-30T13:25:00Z");

        PlatformLinkChallengeCreated robloxChallenge = new RobloxPlatformAccountBindingHandler(linkHandler).createRobloxLinkChallenge(playerId, Duration.ofMinutes(5), now);
        PlatformAccountBinding robloxBinding = new RobloxPlatformAccountBindingHandler(linkHandler).bindRobloxAccount(robloxChallenge.challenge().challengeId(), robloxChallenge.shortCode(), "98765", "RobloxUser", now.plusSeconds(1));
        assertEquals(GamePlatform.ROBLOX, robloxBinding.platform());

        PlatformLinkChallengeCreated discordChallenge = new DiscordPlatformAccountBindingHandler(linkHandler).createDiscordLinkChallenge(playerId, Duration.ofMinutes(5), now.plusSeconds(2));
        PlatformAccountBinding discordBinding = new DiscordPlatformAccountBindingHandler(linkHandler).bindDiscordAccount(discordChallenge.challenge().challengeId(), discordChallenge.shortCode(), "discord-98765", "DiscordUser", now.plusSeconds(3));
        assertEquals(GamePlatform.DISCORD, discordBinding.platform());

        PlatformLinkChallengeCreated minecraftChallenge = new MinecraftPlatformAccountBindingHandler(linkHandler).createMinecraftLinkChallenge(playerId, Duration.ofMinutes(5), now.plusSeconds(4));
        PlatformAccountBinding minecraftBinding = new MinecraftPlatformAccountBindingHandler(linkHandler).bindMinecraftAccount(minecraftChallenge.challenge().challengeId(), minecraftChallenge.shortCode(), "minecraft-uuid", "MinecraftUser", now.plusSeconds(5));
        assertEquals(GamePlatform.MINECRAFT, minecraftBinding.platform());

    }
}
