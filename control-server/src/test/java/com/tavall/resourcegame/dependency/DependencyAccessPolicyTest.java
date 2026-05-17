package com.tavall.resourcegame.dependency;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DependencyAccessPolicyTest {
    @Test
    void mainSourceFilesUseDomainAccessorsForTavallDependencyLookups() throws IOException {
        Path repositoryRoot = Path.of("").toAbsolutePath().getParent();
        List<String> violations = new ArrayList<>();

        try (Stream<Path> sourceFiles = Files.walk(repositoryRoot, 8)) {
            sourceFiles
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> path.toString().contains("src\\main\\java"))
                    .filter(path -> !isGeneratedTargetFile(path))
                    .forEach(path -> collectPolicyViolations(path, violations));
        }

        assertTrue(
                violations.isEmpty(),
                "DependencyLoaderAccess must stay inside generated/default accessors or composition modules:%n%s"
                        .formatted(String.join(System.lineSeparator(), violations))
        );
    }

    /**
     * This keeps Tavall DI usage discoverable: gameplay/runtime files call default interface
     * methods, while only composition/access files touch the loader directly.
     */
    private void collectPolicyViolations(Path path, List<String> violations) {
        try {
            int lineNumber = 0;
            for (String line : Files.readAllLines(path)) {
                lineNumber++;
                if (!line.contains("DependencyLoaderAccess.")) {
                    continue;
                }
                if (isAllowedLoaderAccess(path)) {
                    continue;
                }
                violations.add(path + ":" + lineNumber + " -> " + line.trim());
            }
        } catch (IOException ex) {
            violations.add(path + " -> failed to read: " + ex.getMessage());
        }
    }

    private boolean isGeneratedTargetFile(Path path) {
        String normalized = path.toString();
        return normalized.contains("\\target\\")
                || normalized.contains("\\.codex-temp\\")
                || normalized.contains("\\temp\\");
    }

    private boolean isAllowedLoaderAccess(Path path) {
        String fileName = path.getFileName().toString();
        String normalized = path.toString();
        return fileName.equals("DependencyLoaderAccess.java")
                || fileName.equals("DependencyInjectorHelper.java")
                || fileName.endsWith("DomainGenerated.java")
                || fileName.endsWith("DependencyModule.java")
                || fileName.equals("ControlCommandRuntimeFactory.java")
                || normalized.contains("\\src\\main\\java\\com\\tavall\\hytale\\resourcegame\\dependency\\");
    }
}
