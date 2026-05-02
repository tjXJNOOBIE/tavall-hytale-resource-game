package com.tavall.hytale.resourcegame.frontend.discord;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class DiscordFrontendModuleTest {
    @Test
    void discordFrontendIsAThinPlatformAdapter() {
        DiscordFrontendModule module = new DiscordFrontendModule();

        assertEquals("tavall-resource-game-discord-frontend", module.moduleName());
        assertEquals("DISCORD", module.platformKey());
        assertEquals("ControlCommandDispatchHandler", module.commandPipelineEntryPoint());
        assertFalse(module.ownsCanonicalGameplayState());
    }
}
