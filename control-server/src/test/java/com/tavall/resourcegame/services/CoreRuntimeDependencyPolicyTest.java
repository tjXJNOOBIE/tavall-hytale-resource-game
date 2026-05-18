package org.tavall.control.runtime;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

final class CoreRuntimeDependencyPolicyTest {
    @Test
    void coreRuntimeFilesDoNotReachIntoDependencyLoaderDirectly() throws IOException {
        assertNoDirectLoaderAccess(Path.of("src/main/java/com/tavall/resourcegame/services"));
        assertNoDirectLoaderAccess(Path.of("src/main/java/com/tavall/resourcegame/ui"));
    }

    /**
     * Runtime code should consume generated/default accessors so wiring remains replaceable by
     * Tavall DI modules instead of being hard-bound to the global dependency loader.
     */
    private static void assertNoDirectLoaderAccess(Path packagePath) throws IOException {
        try (var paths = Files.walk(packagePath)) {
            for (Path path : paths.filter(path -> path.toString().endsWith(".java")).toList()) {
                String source = Files.readString(path);
                assertFalse(source.contains("DependencyLoaderAccess."), path + " should use generated/default DI accessors.");
            }
        }
    }
}
