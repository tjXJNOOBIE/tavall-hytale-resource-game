package org.tavall.api.minecraft.frontend;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FrontendCommandEnvelopeTest {

    @Test
    void commandEnvelopeCarriesPlatformCommandInput() {
        FrontendCommandEnvelope envelope = FrontendCommandEnvelope.command(
                ResourceGameFrontendPlatform.HYTALE,
                "hytale-player-1",
                "ScoutOne",
                "/kd castle info",
                "corr-1",
                Map.of("worldId", "test-world")
        );

        assertEquals(ResourceGameFrontendPlatform.HYTALE, envelope.platform());
        assertEquals(FrontendCommandSurface.COMMAND, envelope.surface());
        assertEquals("/kd castle info", envelope.rawInput());
        assertEquals("test-world", envelope.sourceMetadata().get("worldId"));
    }

    @Test
    void actionEnvelopeCarriesTypedActionArguments() {
        FrontendCommandEnvelope envelope = FrontendCommandEnvelope.action(
                ResourceGameFrontendPlatform.ROBLOX,
                FrontendCommandSurface.REMOTE_EVENT,
                "roblox-42",
                "Courier",
                "action.trade_route.inspect",
                Map.of("routeId", "route-1"),
                "corr-2",
                Map.of("serverId", "roblox-shard")
        );

        assertEquals("action.trade_route.inspect", envelope.actionId());
        assertEquals("route-1", envelope.arguments().get("routeId"));
        assertEquals("roblox-shard", envelope.sourceMetadata().get("serverId"));
    }

    @Test
    void envelopeRequiresRawInputOrActionId() {
        assertThrows(IllegalArgumentException.class, () -> new FrontendCommandEnvelope(
                ResourceGameFrontendPlatform.DISCORD,
                FrontendCommandSurface.DISCORD_INTERACTION,
                "discord-1",
                "Operator",
                null,
                null,
                Map.of(),
                "corr-3",
                Map.of()
        ));
    }
}
