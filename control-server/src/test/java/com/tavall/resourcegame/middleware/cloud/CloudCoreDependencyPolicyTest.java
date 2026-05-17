package com.tavall.resourcegame.middleware.cloud;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class CloudCoreDependencyPolicyTest {
    private static final Pattern APP_LOGIC_INJECTION = Pattern.compile(
            "(private\\s+final\\s+(?!Map<)[^;]*(Repository|Handler|Runtime|Adapter|Service|Config|Mapper|Hasher)\\s+\\w+\\s*;)"
                    + "|(public\\s+\\w+\\s*\\([^)]*(Repository|Handler|Runtime|Adapter|Service|Config|Mapper|Hasher)[^)]*\\))"
    );

    @Test
    void cloudCoreDoesNotDependOnSpringOrControlServerAdapters() throws IOException {
        Path sourceRoot = Path.of("src/main/java/com/tavall/resourcegame/middleware/cloud").toAbsolutePath();
        List<String> violations = new ArrayList<>();

        try (Stream<Path> sourceFiles = Files.walk(sourceRoot)) {
            sourceFiles
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> collectForbiddenImports(path, violations));
        }

        assertTrue(
                violations.isEmpty(),
                "Cloud core main code must stay plain Java and adapter-free:%n%s"
                        .formatted(String.join(System.lineSeparator(), violations))
        );
    }

    @Test
    void cloudCoreDoesNotIntroduceServiceClassesOrInjectedCollaborators() throws IOException {
        Path sourceRoot = Path.of("src/main/java/com/tavall/resourcegame/middleware/cloud").toAbsolutePath();
        List<String> violations = new ArrayList<>();

        try (Stream<Path> sourceFiles = Files.walk(sourceRoot)) {
            sourceFiles
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> collectDependencyPolicyViolations(path, violations));
        }

        assertTrue(
                violations.isEmpty(),
                "Cloud core must avoid Service classes and app-logic constructor/field injection:%n%s"
                        .formatted(String.join(System.lineSeparator(), violations))
        );
    }

    private void collectForbiddenImports(Path path, List<String> violations) {
        try {
            int lineNumber = 0;
            for (String line : Files.readAllLines(path)) {
                lineNumber++;
                if (line.contains("org.springframework") || line.contains("resourcegame.controlserver")) {
                    violations.add(path + ":" + lineNumber + " -> " + line.trim());
                }
            }
        } catch (IOException ex) {
            violations.add(path + " -> failed to read: " + ex.getMessage());
        }
    }

    private void collectDependencyPolicyViolations(Path path, List<String> violations) {
        try {
            if (path.getFileName().toString().endsWith("Service.java")) {
                violations.add(path.toString());
                return;
            }
            int lineNumber = 0;
            for (String line : Files.readAllLines(path)) {
                lineNumber++;
                if (APP_LOGIC_INJECTION.matcher(line).find()) {
                    violations.add(path + ":" + lineNumber + " -> " + line.trim());
                }
            }
        } catch (IOException ex) {
            violations.add(path + " -> failed to read: " + ex.getMessage());
        }
    }
}
