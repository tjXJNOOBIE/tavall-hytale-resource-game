package com.tavall.hytale.resourcegame.frontend.roblox;

import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendModuleDescriptor;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendRuntime;

public final class RobloxFrontendModule {
    private static final ResourceGameFrontendModuleDescriptor DESCRIPTOR = new ResourceGameFrontendModuleDescriptor(
            "tavall-resource-game-roblox-frontend",
            ResourceGameFrontendPlatform.ROBLOX,
            ResourceGameFrontendRuntime.ROBLOX_LUAU,
            "FrontendCommandIngressHandler",
            false
    );

    public ResourceGameFrontendModuleDescriptor descriptor() {
        return DESCRIPTOR;
    }

    public String moduleName() {
        return DESCRIPTOR.moduleName();
    }

    public String platformKey() {
        return DESCRIPTOR.platformKey();
    }

    public String implementationRuntime() {
        return DESCRIPTOR.runtime().name();
    }

    public boolean ownsCanonicalGameplayState() {
        return DESCRIPTOR.ownsCanonicalGameplayState();
    }
}
