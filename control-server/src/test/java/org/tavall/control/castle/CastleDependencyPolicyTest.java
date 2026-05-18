package org.tavall.control.castle;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;

final class CastleDependencyPolicyTest {
    @Test
    void castleHandlersUseDomainAccessorsInsteadOfCachedCollaborators() throws IOException {
        assertPackagePolicy(Path.of("src/main/java/org/tavall/control/castle"), "ICastleDomain");
    }

    private static void assertPackagePolicy(Path packagePath, String domainInterface) throws IOException {
        try (var paths = Files.walk(packagePath)) {
            List<Path> javaFiles = paths.filter(path -> path.toString().endsWith(".java")).toList();
            for (Path path : javaFiles) {
                String source = Files.readString(path);
                if (!path.getFileName().toString().contains("DomainGenerated")) {
                    assertFalse(source.contains("DependencyLoaderAccess."), path + " should not access the dependency loader directly.");
                }
                assertFalse(path.getFileName().toString().endsWith("Service.java"), "No new Service classes in " + packagePath);
            }
        }
    }
}
