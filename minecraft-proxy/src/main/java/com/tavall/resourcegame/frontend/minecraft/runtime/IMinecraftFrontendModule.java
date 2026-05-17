package com.tavall.resourcegame.frontend.minecraft.runtime;

import com.tavall.resourcegame.api.internal.frontend.ResourceGameFrontendModuleDescriptor;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftFrontendModule extends IDependencyInjectableInterface {
    ResourceGameFrontendModuleDescriptor descriptor();

    String moduleName();

    String platformKey();

    String implementationLanguage();

    boolean ownsCanonicalGameplayState();

    String commandPipelineEntryPoint();
}
