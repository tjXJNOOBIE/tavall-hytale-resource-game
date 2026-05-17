package com.tavall.hytale.resourcegame.frontend.hytale;

import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendModuleDescriptor;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendRuntime;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendSurfaceIdentity;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

public final class HytaleFrontendModule implements IHytaleFrontendModule, IDependencyInjectableConcrete {
    private static final ResourceGameFrontendModuleDescriptor DESCRIPTOR = new ResourceGameFrontendModuleDescriptor(
            "hytale-frontend",
            ResourceGameFrontendPlatform.HYTALE,
            ResourceGameFrontendRuntime.HYTALE_NATIVE_JAVA,
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

    public String implementationLanguage() {
        return DESCRIPTOR.runtime().name();
    }

    public boolean ownsCanonicalGameplayState() {
        return DESCRIPTOR.ownsCanonicalGameplayState();
    }

    public String commandPipelineEntryPoint() {
        return DESCRIPTOR.commandPipelineEntryPoint();
    }

    public ResourceGameFrontendSurfaceIdentity surfaceIdentity() {
        return ResourceGameFrontendSurfaceIdentity.HYTALE_SINGLE_SERVER;
    }
}
