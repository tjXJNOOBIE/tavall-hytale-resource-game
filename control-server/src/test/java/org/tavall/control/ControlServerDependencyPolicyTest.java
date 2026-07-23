package org.tavall.control;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ControlServerDependencyPolicyTest {
    private static final Pattern APP_CONSTRUCTOR_INJECTION = Pattern.compile(
            "public\\s+\\w+\\s*\\([^)]*(Runtime|Repository|Operator|Handler|Handler|Client|Template|WebClient)[^)]*\\)"
    );
    private static final Pattern APP_FIELD_INJECTION = Pattern.compile(
            "(@Autowired|private\\s+final\\s+.*(Runtime|Repository|Operator|Handler|Handler|Client|Template|WebClient)\\s+\\w+)"
    );
    private static final Pattern ADAPTER_OWNED_RUNTIME_STATE = Pattern.compile(
            "(ConcurrentHashMap|Map<.*RuntimeSnapshot|Map<.*Cloud|Map<.*Workload|Map<.*Node)"
    );
    private static final Pattern INTERNAL_HTTP_LOOPBACK = Pattern.compile(
            "(RestTemplate|WebClient|HttpClient|HttpURLConnection|localhost|127\\.0\\.0\\.1)"
    );
    private static final Pattern CONTROLLER_VIEW_ASSEMBLY = Pattern.compile(
            "(StringBuilder|for\\s*\\(|while\\s*\\(|\\.forEach\\s*\\()"
    );
    private static final Pattern CONTROLLER_DIRECT_RUNTIME_ACCESS = Pattern.compile(
            "getControlCommandRuntime\\(\\)(?!\\.frontendCommandIngressHandler\\(\\))"
    );

    @Test
    void springAndCliAdaptersUseTavallDomainAccessorsInsteadOfConstructorOrFieldInjection() throws IOException {
        List<String> violations = new ArrayList<>();

        for (Path sourceRoot : adapterSourceRoots()) {
            try (Stream<Path> sourceFiles = Files.walk(sourceRoot)) {
                sourceFiles
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".java"))
                        .filter(path -> path.getFileName().toString().endsWith("Controller.java")
                                || path.getFileName().toString().endsWith("Application.java")
                                || path.getFileName().toString().endsWith("Configuration.java"))
                        .forEach(path -> collectPatternViolations(path, violations, APP_CONSTRUCTOR_INJECTION, APP_FIELD_INJECTION));
            }
        }

        assertTrue(
                violations.isEmpty(),
                "Control-server web and CLI adapters must use Tavall DI default accessors, not constructor/field injection:%n%s"
                        .formatted(String.join(System.lineSeparator(), violations))
        );
    }

    @Test
    void springAndCliAdaptersDoNotOwnHotControlPlaneState() throws IOException {
        List<String> violations = new ArrayList<>();

        for (Path sourceRoot : adapterSourceRoots()) {
            try (Stream<Path> sourceFiles = Files.walk(sourceRoot)) {
                sourceFiles
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".java"))
                        .filter(path -> path.getFileName().toString().endsWith("Controller.java")
                                || path.getFileName().toString().endsWith("Application.java")
                                || path.getFileName().toString().endsWith("Configuration.java"))
                        .forEach(path -> collectPatternViolations(path, violations, ADAPTER_OWNED_RUNTIME_STATE));
            }
        }

        assertTrue(
                violations.isEmpty(),
                "Control-server web and CLI adapters must delegate hot runtime state to plain Java handlers:%n%s"
                        .formatted(String.join(System.lineSeparator(), violations))
        );
    }

    @Test
    void springControllersStayThinAndDelegateRuntimeQueriesToHandlers() throws IOException {
        Path sourceRoot = Path.of("src/main/java/org/tavall/control/web").toAbsolutePath();
        List<String> violations = new ArrayList<>();

        try (Stream<Path> sourceFiles = Files.walk(sourceRoot)) {
            sourceFiles
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectPatternViolations(path, violations, CONTROLLER_VIEW_ASSEMBLY, CONTROLLER_DIRECT_RUNTIME_ACCESS));
        }

        assertTrue(
                violations.isEmpty(),
                "Spring controllers must remain route shims and delegate runtime/view work to Java handlers:%n%s"
                        .formatted(String.join(System.lineSeparator(), violations))
        );
    }

    @Test
    void controllersAndCliDoNotUseInternalHttpLoopback() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path sourceRoot : List.of(
                Path.of("src/main/java/org/tavall/control/web").toAbsolutePath(),
                Path.of("src/main/java/org/tavall/control/cli").toAbsolutePath())) {
            try (Stream<Path> sourceFiles = Files.walk(sourceRoot)) {
                sourceFiles
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".java"))
                        .forEach(path -> collectPatternViolations(path, violations, INTERNAL_HTTP_LOOPBACK));
            }
        }

        assertTrue(
                violations.isEmpty(),
                "Control-server controllers and CLI must call Java handlers directly, not localhost HTTP loopback:%n%s"
                        .formatted(String.join(System.lineSeparator(), violations))
        );
    }

    @Test
    void controlServerBuildDoesNotCompileAgainstEventOrLiveOpsImplementations() throws IOException {
        Path workingDirectory = Path.of("").toAbsolutePath();
        Path buildFile = Files.exists(workingDirectory.resolve("build.gradle.kts"))
                ? workingDirectory.resolve("build.gradle.kts")
                : workingDirectory.getParent().resolve("build.gradle.kts");
        String build = Files.readString(buildFile);

        assertTrue(!build.contains("project(\":events\")"),
                "Control-server should not compile against event implementation module.");
        assertTrue(!build.contains("project(\":liveops\")"),
                "Control-server should not compile against LiveOps implementation module.");
    }

    private void collectPatternViolations(Path path, List<String> violations, Pattern... forbiddenPatterns) {
        try {
            int lineNumber = 0;
            for (String line : Files.readAllLines(path)) {
                lineNumber++;
                for (Pattern forbiddenPattern : forbiddenPatterns) {
                    if (forbiddenPattern.matcher(line).find()) {
                        violations.add(path + ":" + lineNumber + " -> " + line.trim());
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            violations.add(path + " -> failed to read: " + ex.getMessage());
        }
    }

    private List<Path> adapterSourceRoots() {
        return List.of(
                Path.of("src/main/java/org/tavall/control/web").toAbsolutePath(),
                Path.of("src/main/java/org/tavall/control/cli").toAbsolutePath()
        );
    }
}
