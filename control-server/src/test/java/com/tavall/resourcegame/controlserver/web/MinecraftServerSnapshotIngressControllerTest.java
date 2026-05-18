package org.tavall.control.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.tavall.control.ControlServerDependencyModule;
import org.tavall.api.minecraft.MinecraftPlayerRuntimeSnapshot;
import org.tavall.api.minecraft.MinecraftServerRuntimeSnapshot;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendSurfaceIdentity;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class MinecraftServerSnapshotIngressControllerTest {
    @Test
    void acceptsAndReturnsLatestBukkitServerSnapshot() throws Exception {
        new ControlServerDependencyModule().registerDependencies();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MinecraftServerSnapshotIngressController()).build();
        MinecraftServerRuntimeSnapshot snapshot = snapshot();

        mockMvc.perform(post("/api/frontend/minecraft/server-snapshots")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(snapshot)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Minecraft server snapshot accepted")));

        mockMvc.perform(get("/api/frontend/minecraft/server-snapshots/latest").param("serverId", "ffa"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"serverId\":\"ffa\"")))
                .andExpect(content().string(containsString("\"playerName\":\"Miner\"")));
    }

    private MinecraftServerRuntimeSnapshot snapshot() {
        return new MinecraftServerRuntimeSnapshot(
                ResourceGameFrontendSurfaceIdentity.BUKKIT_SERVER,
                "ffa",
                "velocity-proxy",
                "minecraft-host",
                1000L,
                2000L,
                1,
                100,
                List.of(new MinecraftPlayerRuntimeSnapshot(
                        UUID.fromString("00000000-0000-0000-0000-000000000001"),
                        "Miner",
                        "world",
                        1.0d,
                        64.0d,
                        2.0d,
                        90.0f,
                        20.0f,
                        18.0d,
                        20,
                        "SURVIVAL",
                        true,
                        Map.of()
                )),
                Map.of("runtime", "spigot")
        );
    }
}
