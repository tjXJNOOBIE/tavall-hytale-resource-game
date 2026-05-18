package org.tavall.api.minecraft.frontend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ResourceGameFrontendModuleDescriptorTest {
    @Test
    void descriptorPreservesPlatformAdapterBoundary() {
        ResourceGameFrontendModuleDescriptor descriptor = new ResourceGameFrontendModuleDescriptor(
                "minecraft-frontend",
                ResourceGameFrontendPlatform.MINECRAFT,
                ResourceGameFrontendRuntime.MINECRAFT_JAVA_PLUGIN,
                "FrontendCommandIngressHandler",
                false
        );

        assertEquals("MINECRAFT", descriptor.platformKey());
        assertFalse(descriptor.ownsCanonicalGameplayState());
    }

    @Test
    void descriptorRejectsBlankCommandPipelineEntryPoint() {
        assertThrows(IllegalArgumentException.class, () -> new ResourceGameFrontendModuleDescriptor(
                "minecraft-frontend",
                ResourceGameFrontendPlatform.MINECRAFT,
                ResourceGameFrontendRuntime.MINECRAFT_JAVA_PLUGIN,
                " ",
                false
        ));
    }
}
