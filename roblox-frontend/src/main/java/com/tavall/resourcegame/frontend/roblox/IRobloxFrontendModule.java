package com.tavall.resourcegame.frontend.roblox;

import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendModuleDescriptor;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IRobloxFrontendModule extends IDependencyInjectableInterface {
    ResourceGameFrontendModuleDescriptor descriptor();

    String moduleName();

    String platformKey();

    String implementationRuntime();

    boolean ownsCanonicalGameplayState();
}
