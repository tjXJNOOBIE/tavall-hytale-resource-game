package com.tavall.hytale.resourcegame.frontend.discord;

import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandSurface;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendRuntime;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class DiscordFrontendModuleTest {
    @Test
    void discordFrontendIsAThinPlatformAdapter() {
        DiscordFrontendModule module = new DiscordFrontendModule();

        assertEquals("tavall-resource-game-discord-frontend", module.moduleName());
        assertEquals("DISCORD", module.platformKey());
        assertEquals(ResourceGameFrontendPlatform.DISCORD, module.descriptor().platform());
        assertEquals(ResourceGameFrontendRuntime.DISCORD_JAVA_BOT, module.descriptor().runtime());
        assertEquals("FrontendCommandIngressHandler", module.commandPipelineEntryPoint());
        assertFalse(module.ownsCanonicalGameplayState());
    }

    @Test
    void discordInteractionEnvelopeTargetsControlIngress() {
        DiscordFrontendCommandEnvelopeFactory factory = new DiscordFrontendCommandEnvelopeFactory();

        FrontendCommandEnvelope envelope = factory.actionEnvelope(
                FrontendCommandSurface.DISCORD_INTERACTION,
                "discord-user",
                "Treasurer",
                "action.black_market.purchase_listing",
                Map.of("listingId", "listing-1"),
                "corr-discord",
                Map.of("guildId", "discord-guild")
        );

        assertEquals(ResourceGameFrontendPlatform.DISCORD, envelope.platform());
        assertEquals("listing-1", envelope.arguments().get("listingId"));
    }

    @Test
    void discordKdBridgeCreatesSlashCommandEnvelope() {
        DiscordKdCommandEnvelopeBridge bridge = new DiscordKdCommandEnvelopeBridge();

        FrontendCommandEnvelope envelope = bridge.commandEnvelope(
                "discord-user",
                "Treasurer",
                java.util.List.of("kingdom", "resources", "give", "player-1", "resource.food.rations", "10"),
                "corr-discord-kd",
                Map.of("guildId", "discord-guild")
        );

        assertEquals(ResourceGameFrontendPlatform.DISCORD, envelope.platform());
        assertEquals("/kingdom resources give player-1 resource.food.rations 10", envelope.rawInput());
        assertEquals("discord-guild", envelope.sourceMetadata().get("guildId"));
    }
}
