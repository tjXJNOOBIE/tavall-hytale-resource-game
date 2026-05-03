package com.tavall.hytale.resourcegame.frontend.hytale;

import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendRuntime;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelope;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class HytaleFrontendModuleTest {
    @Test
    void hytaleFrontendIsAThinPlatformAdapter() {
        HytaleFrontendModule module = new HytaleFrontendModule();

        assertEquals("tavall-resource-game-hytale-frontend", module.moduleName());
        assertEquals("HYTALE", module.platformKey());
        assertEquals(ResourceGameFrontendPlatform.HYTALE, module.descriptor().platform());
        assertEquals(ResourceGameFrontendRuntime.HYTALE_NATIVE_JAVA, module.descriptor().runtime());
        assertEquals("FrontendCommandIngressHandler", module.commandPipelineEntryPoint());
        assertFalse(module.ownsCanonicalGameplayState());
    }

    @Test
    void hytaleCommandEnvelopeTargetsControlIngress() {
        HytaleFrontendCommandEnvelopeFactory factory = new HytaleFrontendCommandEnvelopeFactory();

        FrontendCommandEnvelope envelope = factory.commandEnvelope(
                "hytale-player",
                "Builder",
                "/kd ui castle-main",
                "corr-hytale",
                Map.of("server", "hytale-dev")
        );

        assertEquals(ResourceGameFrontendPlatform.HYTALE, envelope.platform());
        assertEquals("/kd ui castle-main", envelope.rawInput());
    }

    @Test
    void kdBridgePreservesNativeCommandAsControlEnvelope() {
        HytaleKdCommandEnvelopeBridge bridge = new HytaleKdCommandEnvelopeBridge();

        FrontendCommandEnvelope envelope = bridge.commandEnvelope(
                "hytale-player",
                "Builder",
                java.util.List.of("resources", "add", "food", "10"),
                "corr-kd-resources",
                Map.of("server", "hytale-dev")
        );

        assertEquals(ResourceGameFrontendPlatform.HYTALE, envelope.platform());
        assertEquals("/kd resources add food 10", envelope.rawInput());
        assertEquals("hytale-dev", envelope.sourceMetadata().get("server"));
    }
}
