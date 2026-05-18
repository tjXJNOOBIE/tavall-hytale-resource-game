package org.tavall.control.healing;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class HealingDependencyPolicyTest {
    @Test
    void healingMiddlewareUsesDomainAccessorsInsteadOfCachedCollaborators() throws IOException {
        Path sourceRoot = sourceRoot("src/main/java/org/tavall/control/healing");
        Pattern collaboratorField = Pattern.compile(
                "private\\s+final\\s+.*(Repository|Handler|Handler|Registry|Resolver|System).*;"
        );
        Pattern directLoaderAccess = Pattern.compile("DependencyLoaderAccess\\.");
        List<String> violations = new ArrayList<>();

        try (Stream<Path> sourceFiles = Files.walk(sourceRoot)) {
            sourceFiles
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> collectViolations(path, collaboratorField, directLoaderAccess, violations));
        }

        assertTrue(
                violations.isEmpty(),
                "Healing middleware should use default domain accessors:%n%s"
                        .formatted(String.join(System.lineSeparator(), violations))
        );
    }

    private void collectViolations(
            Path path,
            Pattern collaboratorField,
            Pattern directLoaderAccess,
            List<String> violations
    ) {
        try {
            String fileName = path.getFileName().toString();
            int lineNumber = 0;
            for (String line : Files.readAllLines(path)) {
                lineNumber++;
                if (collaboratorField.matcher(line).find()) {
                    violations.add(path + ":" + lineNumber + " -> " + line.trim());
                    continue;
                }
                if (directLoaderAccess.matcher(line).find() && !fileName.endsWith("DomainGenerated.java")) {
                    violations.add(path + ":" + lineNumber + " -> " + line.trim());
                }
            }
        } catch (IOException ex) {
            violations.add(path + " -> failed to read: " + ex.getMessage());
        }
    }

    private Path sourceRoot(String moduleRelativePath) {
        Path modulePath = Path.of(moduleRelativePath);
        if (Files.exists(modulePath)) {
            return modulePath;
        }
        return Path.of("core").resolve(moduleRelativePath);
    }
}
