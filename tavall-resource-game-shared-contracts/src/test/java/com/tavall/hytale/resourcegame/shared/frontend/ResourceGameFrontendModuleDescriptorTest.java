package com.tavall.hytale.resourcegame.shared.frontend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ResourceGameFrontendModuleDescriptorTest {
    @Test
    void descriptorPreservesPlatformAdapterBoundary() {
        ResourceGameFrontendModuleDescriptor descriptor = new ResourceGameFrontendModuleDescriptor(
                "tavall-resource-game-hytale-frontend",
                ResourceGameFrontendPlatform.HYTALE,
                ResourceGameFrontendRuntime.HYTALE_NATIVE_JAVA,
                "ControlCommandDispatchHandler",
                false
        );

        assertEquals("HYTALE", descriptor.platformKey());
        assertFalse(descriptor.ownsCanonicalGameplayState());
    }

    @Test
    void descriptorRejectsBlankCommandPipelineEntryPoint() {
        assertThrows(IllegalArgumentException.class, () -> new ResourceGameFrontendModuleDescriptor(
                "tavall-resource-game-hytale-frontend",
                ResourceGameFrontendPlatform.HYTALE,
                ResourceGameFrontendRuntime.HYTALE_NATIVE_JAVA,
                " ",
                false
        ));
    }
}
