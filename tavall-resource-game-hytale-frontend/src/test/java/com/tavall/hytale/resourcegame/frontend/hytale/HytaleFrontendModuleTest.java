package com.tavall.hytale.resourcegame.frontend.hytale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class HytaleFrontendModuleTest {
    @Test
    void hytaleFrontendIsAThinPlatformAdapter() {
        HytaleFrontendModule module = new HytaleFrontendModule();

        assertEquals("tavall-resource-game-hytale-frontend", module.moduleName());
        assertEquals("HYTALE", module.platformKey());
        assertEquals("ControlCommandDispatchHandler", module.commandPipelineEntryPoint());
        assertFalse(module.ownsCanonicalGameplayState());
    }
}
