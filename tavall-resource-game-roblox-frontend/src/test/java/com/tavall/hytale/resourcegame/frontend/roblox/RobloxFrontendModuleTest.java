package com.tavall.hytale.resourcegame.frontend.roblox;

import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendRuntime;
import org.junit.jupiter.api.Test;

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
        assertFalse(module.ownsCanonicalGameplayState());
    }
}
