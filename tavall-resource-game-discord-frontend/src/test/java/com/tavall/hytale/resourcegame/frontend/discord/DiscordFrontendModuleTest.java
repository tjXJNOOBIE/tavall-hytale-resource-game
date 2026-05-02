package com.tavall.hytale.resourcegame.frontend.discord;

import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class DiscordFrontendModuleTest {
    @Test
    void discordFrontendIsAThinPlatformAdapter() {
        DiscordFrontendModule module = new DiscordFrontendModule();

        assertEquals("tavall-resource-game-discord-frontend", module.moduleName());
        assertEquals("DISCORD", module.platformKey());
        assertEquals(ResourceGameFrontendPlatform.DISCORD, module.descriptor().platform());
        assertEquals(ResourceGameFrontendRuntime.DISCORD_JAVA_BOT, module.descriptor().runtime());
        assertEquals("ControlCommandDispatchHandler", module.commandPipelineEntryPoint());
        assertFalse(module.ownsCanonicalGameplayState());
    }
}
