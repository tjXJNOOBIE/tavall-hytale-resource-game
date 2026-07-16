package org.tavall.control.runtime;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

final class CoreRuntimeDependencyPolicyTest {
    @Test
    void coreRuntimeFilesDoNotReachIntoDependencyLoaderDirectly() throws IOException {
        assertNoDirectLoaderAccess(Path.of("src/main/java/org/tavall/control/runtime"));
        assertNoDirectLoaderAccess(Path.of("src/main/java/org/tavall/control/api/UIData.java"));
    }

    /**
     * Runtime code should consume generated/default accessors so wiring remains replaceable by
     * Tavall DI modules instead of being hard-bound to the global dependency loader.
     */
    private static void assertNoDirectLoaderAccess(Path packagePath) throws IOException {
        if (packagePath.toString().endsWith(".java")) {
            String source = Files.readString(packagePath);
            assertFalse(source.contains("DependencyLoaderAccess."), packagePath + " should use generated/default DI accessors.");
            return;
        }
        try (var paths = Files.walk(packagePath)) {
            for (Path path : paths.filter(path -> path.toString().endsWith(".java")).toList()) {
                if (path.getFileName().toString().equals("ControlCommandRuntimeFactory.java")
                        || path.getFileName().toString().equals("ControlCommandDomain.java")) {
                    continue;
                }
                String source = Files.readString(path);
                assertFalse(source.contains("DependencyLoaderAccess."), path + " should use generated/default DI accessors.");
            }
        }
    }
}
