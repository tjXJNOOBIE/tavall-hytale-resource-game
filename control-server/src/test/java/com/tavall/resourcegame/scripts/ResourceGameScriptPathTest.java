package org.tavall.control.scripts;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ResourceGameScriptPathTest {
    private static final Pattern ROOT_HYTALE_PLUGIN_JAR_PATH = Pattern.compile(
            "(?<!core[\\\\/])target[\\\\/]tavall-hytale-resource-game\\.jar"
    );
    private static final Pattern DIRECT_CONTROL_SERVER_MAIN_LAUNCH = Pattern.compile(
            "java\\s+.*-cp\\s+.*ControlServerApplication"
    );

    @Test
    void scriptsUseModuleArtifactsInsteadOfLegacyRootArtifacts() throws IOException {
        Path scriptDirectory = resourceGameRoot().resolve("scripts");
        List<String> failures = new ArrayList<>();
        try (Stream<Path> scriptPaths = Files.list(scriptDirectory)) {
            for (Path scriptPath : scriptPaths.filter(path -> path.getFileName().toString().endsWith(".ps1")).toList()) {
                List<String> lines = Files.readAllLines(scriptPath);
                for (int index = 0; index < lines.size(); index++) {
                    String line = lines.get(index);
                    if (ROOT_HYTALE_PLUGIN_JAR_PATH.matcher(line).find()) {
                        failures.add(scriptPath.getFileName() + ":" + (index + 1) + " uses the legacy root Hytale plugin jar path.");
                    }
                    if (line.contains("core\\core")
                            || line.contains("core/core")) {
                        failures.add(scriptPath.getFileName() + ":" + (index + 1) + " duplicates the core module path.");
                    }
                    if (line.contains("0.1.0-SNAPSHOT")) {
                        failures.add(scriptPath.getFileName() + ":" + (index + 1) + " still references a 0.1.0 snapshot artifact.");
                    }
                    if (DIRECT_CONTROL_SERVER_MAIN_LAUNCH.matcher(line).find()) {
                        failures.add(scriptPath.getFileName() + ":" + (index + 1) + " launches the control server main class directly instead of the executable jar.");
                    }
                }
            }
        }

        assertTrue(failures.isEmpty(), () -> String.join(System.lineSeparator(), failures));
    }

    /**
     * Maven runs module tests with different working directories depending on the invocation,
     * so the script scan resolves either the aggregator root or the current module root.
     */
    private static Path resourceGameRoot() {
        Path workingDirectory = Path.of("").toAbsolutePath();
        if (Files.isDirectory(workingDirectory.resolve("scripts"))) {
            return workingDirectory;
        }
        return workingDirectory.getParent();
    }
}
