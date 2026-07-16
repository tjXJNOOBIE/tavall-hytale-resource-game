package org.tavall.api.minecraft;

import org.tavall.api.minecraft.frontend.FrontendCommandEnvelope;
import org.tavall.api.minecraft.frontend.FrontendCommandSurface;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendSurfaceIdentity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class MinecraftSurfaceContractTest {
    @Test
    void serverRuntimeSnapshotCarriesBackendOnlyPlayerData() throws Exception {
        MinecraftPlayerRuntimeSnapshot player = new MinecraftPlayerRuntimeSnapshot(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Miner",
                "world",
                1.0d,
                64.0d,
                2.0d,
                90.0f,
                20.0f,
                18.5d,
                19,
                "SURVIVAL",
                true,
                Map.of("surface", "server")
        );
        MinecraftServerRuntimeSnapshot snapshot = new MinecraftServerRuntimeSnapshot(
                ResourceGameFrontendSurfaceIdentity.BUKKIT_SERVER,
                "ffa",
                "velocity-proxy",
                "minecraft-host",
                1000L,
                2000L,
                1,
                100,
                List.of(player),
                Map.of("runtime", "spigot")
        );

        String json = new ObjectMapper().writeValueAsString(snapshot);
        MinecraftServerRuntimeSnapshot restored = new ObjectMapper().readValue(json, MinecraftServerRuntimeSnapshot.class);

        assertEquals(ResourceGameFrontendSurfaceIdentity.BUKKIT_SERVER, restored.surfaceIdentity());
        assertEquals("ffa", restored.serverId());
        assertEquals("Miner", restored.players().get(0).playerName());
        assertEquals("spigot", restored.metadata().get("runtime"));
    }

    @Test
    void minecraftInteractionEnvelopeConvertsToFrontendCommandEnvelope() {
        MinecraftInteractionEnvelope interaction = new MinecraftInteractionEnvelope(
                ResourceGameFrontendSurfaceIdentity.BUKKIT_SERVER,
                "ffa",
                "minecraft-player",
                "Miner",
                FrontendCommandSurface.ENTITY_INTERACT,
                "minecraft.entity.interact",
                Map.of("entityType", "VILLAGER"),
                "corr-interact",
                Map.of("serverId", "ffa")
        );

        FrontendCommandEnvelope envelope = interaction.toFrontendCommandEnvelope();

        assertEquals(ResourceGameFrontendPlatform.MINECRAFT, envelope.platform());
        assertEquals(FrontendCommandSurface.ENTITY_INTERACT, envelope.surface());
        assertEquals("minecraft.entity.interact", envelope.actionId());
        assertEquals("VILLAGER", envelope.arguments().get("entityType"));
    }

    @Test
    void visualRenderRequestRequiresTargetSurface() {
        assertThrows(NullPointerException.class, () -> new MinecraftVisualRenderRequest(
                null,
                "ffa",
                "player",
                "CHAT",
                "Title",
                "Body",
                Map.of(),
                "corr-visual"
        ));
    }
}
