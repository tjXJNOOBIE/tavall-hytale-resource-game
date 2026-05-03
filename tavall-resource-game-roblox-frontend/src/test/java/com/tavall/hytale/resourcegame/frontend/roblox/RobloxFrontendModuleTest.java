package com.tavall.hytale.resourcegame.frontend.roblox;

import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandSurface;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendRuntime;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class RobloxFrontendModuleTest {
    @Test
    void robloxFrontendIsALuauAdapterBackedBySharedContracts() {
        RobloxFrontendModule module = new RobloxFrontendModule();

        assertEquals("tavall-resource-game-roblox-frontend", module.moduleName());
        assertEquals("ROBLOX", module.platformKey());
        assertEquals(ResourceGameFrontendPlatform.ROBLOX, module.descriptor().platform());
        assertEquals(ResourceGameFrontendRuntime.ROBLOX_LUAU, module.descriptor().runtime());
        assertEquals("FrontendCommandIngressHandler", module.descriptor().commandPipelineEntryPoint());
        assertFalse(module.ownsCanonicalGameplayState());
    }

    @Test
    void robloxRemoteEventEnvelopeTargetsControlIngress() {
        RobloxFrontendCommandEnvelopeFactory factory = new RobloxFrontendCommandEnvelopeFactory();

        FrontendCommandEnvelope envelope = factory.actionEnvelope(
                FrontendCommandSurface.REMOTE_EVENT,
                "roblox-user",
                "Courier",
                "action.trade_route.inspect",
                Map.of("routeId", "route-1"),
                "corr-roblox",
                Map.of("placeId", "kingdoms")
        );

        assertEquals(ResourceGameFrontendPlatform.ROBLOX, envelope.platform());
        assertEquals("route-1", envelope.arguments().get("routeId"));
    }

    @Test
    void robloxKdBridgeCreatesRemoteCommandEnvelope() {
        RobloxKdCommandEnvelopeBridge bridge = new RobloxKdCommandEnvelopeBridge();

        FrontendCommandEnvelope envelope = bridge.commandEnvelope(
                "roblox-user",
                "Courier",
                java.util.List.of("market", "listing", "inspect", "listing-1"),
                "corr-roblox-kd",
                Map.of("placeId", "kingdoms")
        );

        assertEquals(ResourceGameFrontendPlatform.ROBLOX, envelope.platform());
        assertEquals("/kd market listing inspect listing-1", envelope.rawInput());
        assertEquals("kingdoms", envelope.sourceMetadata().get("placeId"));
    }
}
