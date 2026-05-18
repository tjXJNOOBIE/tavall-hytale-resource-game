package org.tavall.control.distribution;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class DistributionDependencyPolicyTest {
    private static final Pattern HANDLER_CONSTRUCTOR_INJECTION = Pattern.compile(
            "public\\s+\\w+\\s*\\([^)]*Handler[^)]*\\)"
    );
    private static final Pattern HANDLER_FIELD_INJECTION = Pattern.compile(
            "private\\s+final\\s+.*Handler\\s+\\w+"
    );
    private static final Pattern REMOTE_POLICY_CONSTRUCTOR_INJECTION = Pattern.compile(
            "public\\s+RemoteCommandHandler\\s*\\([^)]*RemoteCommandPolicy[^)]*\\)"
    );
    private static final Pattern CONCRETE_HANDLER_REGISTRATION = Pattern.compile(
            "registerInstance\\s*\\(\\s*(?!I)\\w+(Handler|Policy)\\.class"
    );

    @Test
    void distributedRuntimeHandlersUseDomainAccessorsForHandlerDependencies() throws IOException {
        Path sourceRoot = Path.of("src/main/java/com/tavall/resourcegame/distribution").toAbsolutePath();
        List<String> violations = new ArrayList<>();

        try (Stream<Path> sourceFiles = Files.walk(sourceRoot)) {
            sourceFiles
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !isAllowedCompositionFile(path))
                    .forEach(path -> collectViolations(path, violations));
        }

        assertTrue(
                violations.isEmpty(),
                "Distribution handlers must use Tavall DI default accessors for handler dependencies:%n%s"
                        .formatted(String.join(System.lineSeparator(), violations))
        );
    }

    @Test
    void distributionModuleRegistersDependencyInterfaces() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/distribution/DistributionDependencyModule.java"));

        assertTrue(
                !CONCRETE_HANDLER_REGISTRATION.matcher(source).find(),
                "Distribution dependency module must register handler dependencies by generated interface tokens."
        );
    }

    private void collectViolations(Path path, List<String> violations) {
        try {
            int lineNumber = 0;
            for (String line : Files.readAllLines(path)) {
                lineNumber++;
                if (HANDLER_CONSTRUCTOR_INJECTION.matcher(line).find()
                        || HANDLER_FIELD_INJECTION.matcher(line).find()
                        || REMOTE_POLICY_CONSTRUCTOR_INJECTION.matcher(line).find()) {
                    violations.add(path + ":" + lineNumber + " -> " + line.trim());
                }
            }
        } catch (IOException ex) {
            violations.add(path + " -> failed to read: " + ex.getMessage());
        }
    }

    private boolean isAllowedCompositionFile(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.equals("DistributionDependencyModule.java")
                || fileName.equals("IDistributionDomainGenerated.java");
    }
}
