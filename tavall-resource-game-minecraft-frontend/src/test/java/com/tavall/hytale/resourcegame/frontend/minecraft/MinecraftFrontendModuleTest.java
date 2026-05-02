package com.tavall.hytale.resourcegame.frontend.minecraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class MinecraftFrontendModuleTest {
    @Test
    void minecraftFrontendIsAThinPlatformAdapter() {
        MinecraftFrontendModule module = new MinecraftFrontendModule();

        assertEquals("tavall-resource-game-minecraft-frontend", module.moduleName());
        assertEquals("MINECRAFT", module.platformKey());
        assertEquals("ControlCommandDispatchHandler", module.commandPipelineEntryPoint());
        assertFalse(module.ownsCanonicalGameplayState());
    }
}
