package com.tavall.resourcegame.frontend.discord;

import com.tavall.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.shared.frontend.FrontendCommandSurface;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationState;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendRuntime;
import com.tjxjnoobie.api.dependency.DependencyLoader;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class DiscordFrontendModuleTest {
    @AfterEach
    void clearDependencies() {
        DependencyLoader.getDependencyLoader().clear();
    }

    @Test
    void discordFrontendIsAThinPlatformAdapter() {
        DiscordFrontendModule module = new DiscordFrontendModule();

        assertEquals("discord-frontend", module.moduleName());
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
        new DiscordFrontendDependencyModule().registerDependencies(testConfig());
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

    @Test
    void discordControlPlaneBridgeSubmitsKdCommandToClient() {
        AtomicReference<FrontendCommandEnvelope> submittedEnvelope = new AtomicReference<>();
        DependencyLoaderAccess.registerInstance(
                IDiscordControlCommandClient.class,
                new RecordingDiscordControlCommandClient(envelope -> {
                    submittedEnvelope.set(envelope);
                    return new FrontendCommandVerificationResult(
                            envelope,
                            FrontendCommandVerificationState.DISPATCHED,
                            true,
                            "dispatched",
                            "cmd-discord",
                            "COMPLETED",
                            Map.of("controlConsoleInput", "resource give player-1 resource.food.rations 10")
                    );
                })
        );
        new DiscordFrontendDependencyModule().registerDependencies(testConfig());
        DiscordControlPlaneCommandBridge bridge = new DiscordControlPlaneCommandBridge();

        FrontendCommandVerificationResult result = bridge.submitKdCommand(
                "discord-user",
                "Treasurer",
                List.of("kingdom", "resources", "give", "player-1", "resource.food.rations", "10"),
                "corr-discord-submit",
                Map.of("guildId", "discord-guild")
        );

        assertEquals(FrontendCommandVerificationState.DISPATCHED, result.state());
        assertEquals("/kingdom resources give player-1 resource.food.rations 10", submittedEnvelope.get().rawInput());
        assertEquals(ResourceGameFrontendPlatform.DISCORD, submittedEnvelope.get().platform());
    }

    private DiscordBotConfig testConfig() {
        return new DiscordBotConfig(
                "test-token",
                "guild-1",
                "tcp://127.0.0.1:18081",
                DiscordPermissionMapping.empty()
        );
    }
}
