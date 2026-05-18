package org.tavall.minecraft.runtime;

import org.tavall.api.minecraft.frontend.ResourceGameFrontendModuleDescriptor;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftFrontendModule extends IDependencyInjectableInterface {
    ResourceGameFrontendModuleDescriptor descriptor();

    String moduleName();

    String platformKey();

    String implementationLanguage();

    boolean ownsCanonicalGameplayState();

    String commandPipelineEntryPoint();
}
