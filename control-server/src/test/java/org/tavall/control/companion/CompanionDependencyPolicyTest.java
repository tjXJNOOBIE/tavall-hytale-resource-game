package org.tavall.control.companion;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class CompanionDependencyPolicyTest {
    private static final Pattern COLLABORATOR_FIELD = Pattern.compile(
            "private\\s+final\\s+.*(Repository|Handler|Handler|Catalog|Registry|Resolver|System).*;"
    );
    private static final Pattern DIRECT_LOADER_ACCESS = Pattern.compile("DependencyLoaderAccess\\.");

    @Test
    void companionMiddlewareUsesDefaultDomainAccessors() throws IOException {
        Path sourceRoot = Path.of("src/main/java/org/tavall/control/companion");
        List<String> violations = new ArrayList<>();

        try (Stream<Path> sourceFiles = Files.walk(sourceRoot)) {
            sourceFiles
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> collectViolations(path, violations));
        }

        assertTrue(
                violations.isEmpty(),
                "Companion middleware should use default domain accessors instead of cached collaborators:%n%s"
                        .formatted(String.join(System.lineSeparator(), violations))
        );
    }

    private void collectViolations(Path path, List<String> violations) {
        try {
            String fileName = path.getFileName().toString();
            int lineNumber = 0;
            for (String line : Files.readAllLines(path)) {
                lineNumber++;
                if (COLLABORATOR_FIELD.matcher(line).find()) {
                    violations.add(path + ":" + lineNumber + " -> " + line.trim());
                    continue;
                }
                if (DIRECT_LOADER_ACCESS.matcher(line).find() && !fileName.endsWith("Domain.java")) {
                    violations.add(path + ":" + lineNumber + " -> " + line.trim());
                }
            }
        } catch (IOException ex) {
            violations.add(path + " -> failed to read: " + ex.getMessage());
        }
    }
}
