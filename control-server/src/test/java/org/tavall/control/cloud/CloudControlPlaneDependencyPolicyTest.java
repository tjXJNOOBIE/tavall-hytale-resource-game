package org.tavall.control.cloud;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class CloudControlPlaneDependencyPolicyTest {
    private static final Pattern COLLABORATOR_FIELD = Pattern.compile(
            "private\\s+final\\s+(?!Map<)[^;]*(Repository|Handler|Runtime|Adapter|Handler|Config|Mapper|Hasher)\\s+\\w+\\s*;"
    );
    private static final Pattern COLLABORATOR_CONSTRUCTOR = Pattern.compile(
            "public\\s+\\w+\\s*\\([^)]*(Repository|Handler|Runtime|Adapter|Handler|Config|Mapper|Hasher)[^)]*\\)"
    );
    private static final Pattern DIRECT_LOADER_ACCESS = Pattern.compile("DependencyLoaderAccess\\.");

    @Test
    void plainJavaCloudControlPlaneDoesNotDependOnSpringOrControlServerAdapters() throws IOException {
        Path sourceRoot = Path.of("src/main/java/org/tavall/control/cloud").toAbsolutePath();
        List<String> violations = new ArrayList<>();

        try (Stream<Path> sourceFiles = Files.walk(sourceRoot)) {
            sourceFiles
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> collectForbiddenImports(path, violations));
        }

        assertTrue(
                violations.isEmpty(),
                "Cloud control-plane main code must stay plain Java and adapter-free:%n%s"
                        .formatted(String.join(System.lineSeparator(), violations))
        );
    }

    @Test
    void cloudControlPlaneUsesTavallDomainAccessorsInsteadOfCachedCollaborators() throws IOException {
        Path sourceRoot = Path.of("src/main/java/org/tavall/control/cloud").toAbsolutePath();
        List<String> violations = new ArrayList<>();

        try (Stream<Path> sourceFiles = Files.walk(sourceRoot)) {
            sourceFiles
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !isAllowedCompositionFile(path))
                    .forEach(path -> collectDependencyAccessViolations(path, violations));
        }

        assertTrue(
                violations.isEmpty(),
                "Cloud control-plane app logic must use Tavall DI default accessors, not cached collaborators:%n%s"
                        .formatted(String.join(System.lineSeparator(), violations))
        );
    }

    @Test
    void cloudControlPlaneDoesNotIntroduceServiceClasses() throws IOException {
        Path sourceRoot = Path.of("src/main/java/org/tavall/control/cloud").toAbsolutePath();
        List<String> violations = new ArrayList<>();

        try (Stream<Path> sourceFiles = Files.walk(sourceRoot)) {
            sourceFiles
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(fileName -> fileName.endsWith("Service.java"))
                    .forEach(violations::add);
        }

        assertTrue(
                violations.isEmpty(),
                "Cloud control-plane must prefer Handler/Repository/Runtime/Adapter names over Service classes:%n%s"
                        .formatted(String.join(System.lineSeparator(), violations))
        );
    }

    @Test
    void cloudControlPlaneRegistersEveryGeneratedDependencyAccessor() throws IOException {
        Path generatedDomain = Path.of("src/main/java/org/tavall/control/cloud/ICloudControlDomain.java");
        Path dependencyModule = Path.of("src/main/java/org/tavall/control/cloud/CloudControlDependencyModule.java");
        String generatedSource = Files.readString(generatedDomain);
        String moduleSource = Files.readString(dependencyModule);
        List<String> violations = new ArrayList<>();
        Pattern accessorToken = Pattern.compile("findInstance\\((I\\w+\\.class)\\)");

        accessorToken.matcher(generatedSource).results()
                .map(match -> match.group(1))
                .filter(token -> !token.equals("IControlAuthorizationHandler.class"))
                .filter(token -> !moduleSource.contains("registerInstance(" + token))
                .forEach(violations::add);

        assertTrue(
                violations.isEmpty(),
                "Cloud control-plane generated accessors must have module registrations:%n%s"
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

    private void collectDependencyAccessViolations(Path path, List<String> violations) {
        try {
            int lineNumber = 0;
            for (String line : Files.readAllLines(path)) {
                lineNumber++;
                if (COLLABORATOR_FIELD.matcher(line).find()
                        || COLLABORATOR_CONSTRUCTOR.matcher(line).find()
                        || DIRECT_LOADER_ACCESS.matcher(line).find()) {
                    violations.add(path + ":" + lineNumber + " -> " + line.trim());
                }
            }
        } catch (IOException ex) {
            violations.add(path + " -> failed to read: " + ex.getMessage());
        }
    }

    private boolean isAllowedCompositionFile(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.endsWith("DependencyModule.java")
                || fileName.endsWith("Domain.java");
    }
}
