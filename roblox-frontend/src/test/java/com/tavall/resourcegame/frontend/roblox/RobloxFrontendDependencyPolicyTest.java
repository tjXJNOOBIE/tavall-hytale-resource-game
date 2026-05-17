package com.tavall.resourcegame.frontend.roblox;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class RobloxFrontendDependencyPolicyTest {
    @Test
    void robloxSurfaceRegistersOnlyRobloxInterfaceTokens() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/frontend/roblox/RobloxFrontendDependencyModule.java"));

        assertTrue(source.contains("registerIfMissing(IRobloxFrontendModule.class"));
        assertTrue(source.contains("registerIfMissing(IRobloxFrontendCommandEnvelopeFactory.class"));
        assertTrue(source.contains("registerIfMissing(IRobloxKdCommandInputFormatterHandler.class"));
        assertTrue(source.contains("registerIfMissing(IRobloxKdCommandEnvelopeBridge.class"));
        assertTrue(source.contains("registerIfMissing(IRobloxControlCommandClient.class"));
        assertTrue(source.contains("registerIfMissing(IRobloxControlPlaneCommandBridge.class"));
        assertFalse(source.contains("Minecraft"));
        assertFalse(source.contains("Velocity"));
        assertFalse(source.contains("Bukkit"));
        assertFalse(source.contains("Discord"));
    }

    @Test
    void robloxApplicationLogicUsesGeneratedDefaultDependencies() throws IOException {
        List<Path> files = List.of(
                Path.of("src/main/java/com/tavall/resourcegame/frontend/roblox/RobloxControlPlaneCommandBridge.java"),
                Path.of("src/main/java/com/tavall/resourcegame/frontend/roblox/RobloxFrontendCommandEnvelopeFactory.java"),
                Path.of("src/main/java/com/tavall/resourcegame/frontend/roblox/RobloxFrontendModule.java"),
                Path.of("src/main/java/com/tavall/resourcegame/frontend/roblox/RobloxKdCommandEnvelopeBridge.java"),
                Path.of("src/main/java/com/tavall/resourcegame/frontend/roblox/RobloxKdCommandInputFormatterHandler.java"),
                Path.of("src/main/java/com/tavall/resourcegame/frontend/roblox/RobloxNoopControlCommandClient.java")
        );
        Pattern injectedConstructor = Pattern.compile("public\\s+Roblox[A-Za-z0-9]+\\s*\\([^)]*[A-Z][A-Za-z0-9_<>?, ]+\\s+[a-z]");
        Pattern cachedDependencyField = Pattern.compile("private\\s+final\\s+(I?Roblox|HttpClient|ObjectMapper).*;");

        for (Path file : files) {
            String source = Files.readString(file);
            assertFalse(source.contains("@Inject"), file + " should not use framework constructor injection.");
            assertFalse(source.contains("@Autowired"), file + " should not use Spring field or constructor injection.");
            assertFalse(injectedConstructor.matcher(source).find(), file + " should not constructor-wire app dependencies.");
            assertFalse(cachedDependencyField.matcher(source).find(), file + " should not cache app dependencies in fields.");
        }
    }

    @Test
    void robloxDomainDefaultMethodsExposeRobloxRuntimeTokens() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/frontend/roblox/IRobloxFrontendDomainGenerated.java"));

        assertTrue(source.contains("DependencyLoaderAccess.requireInstance(IRobloxFrontendModule.class)"));
        assertTrue(source.contains("DependencyLoaderAccess.requireInstance(IRobloxControlPlaneCommandBridge.class)"));
        assertTrue(source.contains("DependencyLoaderAccess.requireInstance(IRobloxControlCommandClient.class)"));
        assertFalse(source.contains("Minecraft"));
        assertFalse(source.contains("Bukkit"));
        assertFalse(source.contains("Discord"));
    }

    @Test
    void robloxFrontendDoesNotIntroduceServiceClasses() throws IOException {
        try (var paths = Files.walk(Path.of("src/main/java"))) {
            List<Path> serviceClasses = paths
                    .filter(path -> path.getFileName().toString().endsWith("Service.java"))
                    .toList();

            assertTrue(serviceClasses.isEmpty(), "Roblox frontend should keep Handler naming: " + serviceClasses);
        }
    }
}
