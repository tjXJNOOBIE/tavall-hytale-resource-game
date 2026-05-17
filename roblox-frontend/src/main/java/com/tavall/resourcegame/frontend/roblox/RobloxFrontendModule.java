package com.tavall.resourcegame.frontend.roblox;

import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendModuleDescriptor;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendRuntime;

public final class RobloxFrontendModule implements IRobloxFrontendModule, com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete {
    private static final ResourceGameFrontendModuleDescriptor DESCRIPTOR = new ResourceGameFrontendModuleDescriptor(
            "roblox-frontend",
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
