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
}
