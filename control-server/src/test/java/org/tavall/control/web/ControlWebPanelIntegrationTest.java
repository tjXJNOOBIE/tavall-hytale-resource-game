package org.tavall.control.web;

import org.tavall.control.ControlServerDependencyModule;
import org.tavall.control.IControlServerDependencyAccess;
import org.tavall.control.api.PlayerDataApi;
import org.tavall.api.minecraft.backend.rank.RankApi;
import org.tavall.control.common.CanonicalLocation;
import org.tavall.control.runtime.ControlCommandRuntime;
import org.tavall.control.healing.WoundSeverity;
import org.tavall.control.healing.WoundType;
import org.tavall.control.identity.UniversalPlayerId;
import org.tavall.control.troop.Troop;
import org.tavall.control.troop.TroopRegistrationHandler;
import org.tavall.api.minecraft.player.PlayerDataRequest;
import org.tavall.api.minecraft.player.PlayerDataResponse;
import org.tavall.api.minecraft.permissions.RankRequest;
import org.tavall.api.minecraft.permissions.RankResponse;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ControlWebPanelIntegrationTest implements IControlServerDependencyAccess {
    @Test
    void dashboardCommandAuditPlatformAndHealingPagesLoadFromRuntimeState() throws Exception {
        new ControlServerDependencyModule().registerDependencies();
        ControlCommandRuntime runtime = getControlCommandRuntime();
        Troop troop = new TroopRegistrationHandler(runtime.troopRepository())
                .registerTroop(Optional.of(UniversalPlayerId.random()), Optional.empty(), "infantry", 2, new CanonicalLocation("web", 1.0d, 2.0d, 3.0d));
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(get("/control"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Canonical middleware/control server is active")));
        mockMvc.perform(get("/control/cloud"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tavall Cloud")))
                .andExpect(content().string(containsString("Cloud Dashboard")))
                .andExpect(content().string(containsString("Volumes")))
                .andExpect(content().string(containsString("Firewall")))
                .andExpect(content().string(containsString("Proxy Routes")))
                .andExpect(content().string(containsString("DNS Records")));
        mockMvc.perform(get("/control/commands"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Command Console")));
        mockMvc.perform(get("/control/platforms"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("MINECRAFT")))
                .andExpect(content().string(containsString("ROBLOX")))
                .andExpect(content().string(containsString("DISCORD")))
                .andExpect(content().string(containsString("ANDROID")))
                .andExpect(content().string(containsString("PC")))
                .andExpect(content().string(not(containsString("HYTALE"))));
        mockMvc.perform(get("/control/healing").param("troopId", troop.troopId().value().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Active Plan")));
        mockMvc.perform(get("/control/audit"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Audit Logs")));
    }

    @Test
    void commandConsoleDryRunAndExecutePostToSharedDispatchHandler() throws Exception {
        new ControlServerDependencyModule().registerDependencies();
        ControlCommandRuntime runtime = getControlCommandRuntime();
        Troop troop = new TroopRegistrationHandler(runtime.troopRepository())
                .registerTroop(Optional.of(UniversalPlayerId.random()), Optional.empty(), "infantry", 1, new CanonicalLocation("web", 1.0d, 64.0d, 1.0d));
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(post("/control/commands")
                        .param("commandLine", "troop wound " + troop.troopId().value() + " " + WoundType.GENERAL_WOUND + " " + WoundSeverity.MINOR)
                        .param("dryRun", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("DRY_RUN_COMPLETED")));
        mockMvc.perform(post("/control/commands")
                        .param("commandLine", "troop wound " + troop.troopId().value() + " " + WoundType.GENERAL_WOUND + " " + WoundSeverity.MINOR)
                        .param("dryRun", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("COMPLETED")))
                .andExpect(content().string(containsString("projection refreshed")));

        mockMvc.perform(get("/control/healing").param("troopId", troop.troopId().value().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("GENERAL_WOUND")));
    }

    @Test
    void frontendPlayerDataDirectApiProjectionUsesSharedRequest() throws Exception {
        new ControlServerDependencyModule().registerDependencies();
        PlayerDataApi api = getPlayerDataApi();
        PlayerDataResponse response = api.inspect(
                new PlayerDataRequest(
                        "player-data-http-test",
                        UUID.fromString("00000000-0000-0000-0000-000000000001"),
                        "RemotePlayer",
                        "minecraft-single-server",
                        "world",
                        Map.of("surface", "minecraft"),
                        1710000000000L
                ),
                Instant.parse("2026-05-14T00:00:00Z")
        );

        assertTrue(response.success());
        assertEquals("Player account not found.", response.message());
    }

    @Test
    void frontendRankDirectApiProjectionUsesSharedRequest() throws Exception {
        new ControlServerDependencyModule().registerDependencies();
        RankApi api = getRankApi();
        RankResponse response = api.inspect(
                RankRequest.list(
                        "rank-http-test",
                        ResourceGameFrontendPlatform.MINECRAFT,
                        "proxy-operator",
                        "ProxyOperator",
                        Map.of("surface", "velocity"),
                        1710000000000L
                ),
                Instant.parse("2026-05-14T00:00:00Z")
        );

        assertTrue(response.success());
        assertTrue(response.message().contains("Loaded"));
    }

    @Test
    void clockPanelQueriesDirectJavaRuntimeAndSubmitsCommandsThroughDispatcher() throws Exception {
        new ControlServerDependencyModule().registerDependencies();
        ControlCommandRuntime runtime = getControlCommandRuntime();
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(get("/control/clock"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Kingdom Clock")))
                .andExpect(content().string(containsString("plain-java-control-server")));

        mockMvc.perform(post("/control/clock/override")
                        .param("kingdomId", "kingdom-1")
                        .param("time", "22:00")
                        .param("dryRun", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("DRY_RUN_COMPLETED")));
        org.junit.jupiter.api.Assertions.assertTrue(runtime.kingdomClockSystem().getCurrentClockState("kingdom-1").timeOverride().isEmpty());

        mockMvc.perform(post("/control/clock/override")
                        .param("kingdomId", "kingdom-1")
                        .param("time", "22:00")
                        .param("dryRun", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("COMPLETED")));
        org.junit.jupiter.api.Assertions.assertTrue(runtime.kingdomClockSystem().getCurrentClockState("kingdom-1").timeOverride().isPresent());
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(
                new ControlDashboardController(),
                new ControlCloudController(),
                new ControlCommandConsoleController(),
                new ControlPlatformStatusController(),
                new ControlAuditLogController(),
                new ControlTroopHealingController(),
                new ControlPlayerController(),
                new ControlGuildController(),
                new ControlCastleController(),
                new ControlGlobalAssetController(),
                new ControlKingdomClockController(),
                new ControlOperatorController()
        ).build();
    }
}
