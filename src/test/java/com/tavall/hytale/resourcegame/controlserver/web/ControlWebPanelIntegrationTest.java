package com.tavall.hytale.resourcegame.controlserver.web;

import com.tavall.hytale.resourcegame.middleware.common.CanonicalLocation;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntimeFactory;
import com.tavall.hytale.resourcegame.middleware.control.ControlOperator;
import com.tavall.hytale.resourcegame.middleware.healing.WoundSeverity;
import com.tavall.hytale.resourcegame.middleware.healing.WoundType;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;
import com.tavall.hytale.resourcegame.middleware.troop.Troop;
import com.tavall.hytale.resourcegame.middleware.troop.TroopRegistrationHandler;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class ControlWebPanelIntegrationTest {
    @Test
    void dashboardCommandAuditPlatformAndHealingPagesLoadFromRuntimeState() throws Exception {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        Troop troop = new TroopRegistrationHandler(runtime.troopRepository())
                .registerTroop(Optional.of(UniversalPlayerId.random()), Optional.empty(), "infantry", 2, new CanonicalLocation("web", 1.0d, 2.0d, 3.0d));
        MockMvc mockMvc = mockMvc(runtime);

        mockMvc.perform(get("/control"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Canonical middleware/control server is active")));
        mockMvc.perform(get("/control/commands"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Command Console")));
        mockMvc.perform(get("/control/platforms"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("MINECRAFT")))
                .andExpect(content().string(containsString("HYTALE")))
                .andExpect(content().string(containsString("ROBLOX")))
                .andExpect(content().string(containsString("DISCORD")));
        mockMvc.perform(get("/control/healing").param("troopId", troop.troopId().value().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Active Plan")));
        mockMvc.perform(get("/control/audit"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Audit Logs")));
    }

    @Test
    void commandConsoleDryRunAndExecutePostToSharedDispatchHandler() throws Exception {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        Troop troop = new TroopRegistrationHandler(runtime.troopRepository())
                .registerTroop(Optional.of(UniversalPlayerId.random()), Optional.empty(), "infantry", 1, new CanonicalLocation("web", 1.0d, 64.0d, 1.0d));
        MockMvc mockMvc = mockMvc(runtime);

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

    private MockMvc mockMvc(ControlCommandRuntime runtime) {
        ControlOperator webOperator = ControlOperator.localOwner(Instant.now());
        return MockMvcBuilders.standaloneSetup(
                new ControlDashboardController(runtime),
                new ControlCommandConsoleController(runtime, webOperator),
                new ControlPlatformStatusController(runtime),
                new ControlAuditLogController(runtime),
                new ControlTroopHealingController(runtime),
                new ControlPlayerController(runtime),
                new ControlGuildController(),
                new ControlCastleController(),
                new ControlGlobalAssetController(),
                new ControlOperatorController(runtime)
        ).build();
    }
}
