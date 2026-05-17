package com.tavall.resourcegame.controlserver;

import com.tavall.resourcegame.frontend.discord.DiscordKdCommandEnvelopeBridge;
import com.tavall.resourcegame.frontend.discord.DiscordFrontendDependencyModule;
import com.tavall.hytale.resourcegame.frontend.hytale.HytaleFrontendDependencyModule;
import com.tavall.hytale.resourcegame.frontend.hytale.HytaleKdCommandEnvelopeBridge;
import com.tavall.resourcegame.frontend.minecraft.MinecraftFrontendDependencyModule;
import com.tavall.resourcegame.frontend.minecraft.MinecraftKdCommandEnvelopeBridge;
import com.tavall.resourcegame.frontend.roblox.RobloxFrontendDependencyModule;
import com.tavall.resourcegame.frontend.roblox.RobloxKdCommandEnvelopeBridge;
import com.tavall.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationState;
import com.tjxjnoobie.api.dependency.DependencyLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class FrontendModuleCommandIngressIntegrationTest {
    private final ResourceGameControlServerModule controlServerModule = new ResourceGameControlServerModule();

    @AfterEach
    void clearExternalDependencies() {
        DependencyLoader.getDependencyLoader().clear();
    }

    @Test
    void hytaleKdBridgeRoutesLocalUiThroughControlIngress() {
        new HytaleFrontendDependencyModule().registerDependencies();
        ControlCommandRuntime runtime = controlServerModule.createInMemoryRuntime();
        FrontendCommandEnvelope envelope = new HytaleKdCommandEnvelopeBridge().commandEnvelope(
                "hytale-player",
                "Builder",
                List.of("ui", "castle-main"),
                "corr-hytale-ui",
                Map.of("worldId", "hytale-dev")
        );

        FrontendCommandVerificationResult result = runtime.frontendCommandIngressHandler().ingest(envelope, Instant.now());

        assertEquals(FrontendCommandVerificationState.DISPATCHED, result.state());
        assertEquals("frontend kd ui kd ui castle-main", result.metadata().get("controlConsoleInput"));
        assertFalse(runtime.auditLogRepository().findRecentAuditLogs(10).isEmpty());
    }

    @Test
    void minecraftKdBridgeDispatchesTroopDebugThroughControlIngress() {
        new MinecraftFrontendDependencyModule().registerDependencies();
        ControlCommandRuntime runtime = controlServerModule.createInMemoryRuntime();
        FrontendCommandEnvelope envelope = new MinecraftKdCommandEnvelopeBridge().commandEnvelope(
                "minecraft-player",
                "Miner",
                List.of("kd", "troops", "debug", "troop-1"),
                "corr-minecraft-troop",
                Map.of("server", "kingdoms")
        );

        FrontendCommandVerificationResult result = runtime.frontendCommandIngressHandler().ingest(envelope, Instant.now());

        assertEquals(FrontendCommandVerificationState.DISPATCHED, result.state());
        assertEquals("troop debug troop-1", result.metadata().get("controlConsoleInput"));
        assertFalse(runtime.auditLogRepository().findRecentAuditLogs(10).isEmpty());
    }

    @Test
    void robloxKdBridgeRoutesMarketCategoryThroughControlIngress() {
        new RobloxFrontendDependencyModule().registerDependencies();
        ControlCommandRuntime runtime = controlServerModule.createInMemoryRuntime();
        FrontendCommandEnvelope envelope = new RobloxKdCommandEnvelopeBridge().commandEnvelope(
                "roblox-user",
                "Courier",
                List.of("market", "listing", "inspect", "listing-1"),
                "corr-roblox-market",
                Map.of("placeId", "kingdoms")
        );

        FrontendCommandVerificationResult result = runtime.frontendCommandIngressHandler().ingest(envelope, Instant.now());

        assertEquals(FrontendCommandVerificationState.DISPATCHED, result.state());
        assertEquals("frontend kd market kd market listing inspect listing-1", result.metadata().get("controlConsoleInput"));
        assertFalse(runtime.auditLogRepository().findRecentAuditLogs(10).isEmpty());
    }

    @Test
    void discordKdBridgeDispatchesResourceGiveThroughControlIngress() {
        new DiscordFrontendDependencyModule().registerDependencies();
        ControlCommandRuntime runtime = controlServerModule.createInMemoryRuntime();
        FrontendCommandEnvelope envelope = new DiscordKdCommandEnvelopeBridge().commandEnvelope(
                "discord-user",
                "Treasurer",
                List.of("kingdom", "resources", "give", "player-1", "resource.food.rations", "10"),
                "corr-discord-resource",
                Map.of("guildId", "discord-guild")
        );

        FrontendCommandVerificationResult result = runtime.frontendCommandIngressHandler().ingest(envelope, Instant.now());

        assertEquals(FrontendCommandVerificationState.DISPATCHED, result.state());
        assertEquals("resource give player-1 resource.food.rations 10", result.metadata().get("controlConsoleInput"));
        assertFalse(runtime.auditLogRepository().findRecentAuditLogs(10).isEmpty());
    }
}
