package com.tavall.hytale.resourcegame.frontend.hytale;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class HytaleFrontendDependencyPolicyTest {
    @Test
    void hytaleSingleSurfaceRegistersOnlyHytaleInterfaceTokens() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/tavall/hytale/resourcegame/frontend/hytale/HytaleFrontendDependencyModule.java"));

        assertTrue(source.contains("registerIfMissing(IHytaleFrontendConfig.class"));
        assertTrue(source.contains("registerIfMissing(IHytaleFrontendModule.class"));
        assertTrue(source.contains("registerIfMissing(IHytaleFrontendCommandEnvelopeFactory.class"));
        assertTrue(source.contains("registerIfMissing(IHytaleKdCommandInputFormatterHandler.class"));
        assertTrue(source.contains("registerIfMissing(IHytaleKdCommandEnvelopeBridge.class"));
        assertTrue(source.contains("registerIfMissing(IHytaleControlCommandClient.class"));
        assertTrue(source.contains("HytaleTcpControlCommandClient"));
        assertTrue(source.contains("registerIfMissing(IHytaleControlPlaneCommandBridge.class"));
        assertFalse(source.contains("Minecraft"));
        assertFalse(source.contains("Velocity"));
        assertFalse(source.contains("Bukkit"));
        assertFalse(source.contains("Proxy"));
    }

    @Test
    void hytaleApplicationLogicUsesGeneratedDefaultDependencies() throws IOException {
        List<Path> files = List.of(
                Path.of("src/main/java/com/tavall/hytale/resourcegame/frontend/hytale/HytaleControlPlaneCommandBridge.java"),
                Path.of("src/main/java/com/tavall/hytale/resourcegame/frontend/hytale/HytaleFrontendCommandEnvelopeFactory.java"),
                Path.of("src/main/java/com/tavall/hytale/resourcegame/frontend/hytale/HytaleKdCommandEnvelopeBridge.java"),
                Path.of("src/main/java/com/tavall/hytale/resourcegame/frontend/hytale/HytaleKdCommandInputFormatterHandler.java")
        );
        Pattern injectedConstructor = Pattern.compile("public\\s+Hytale[A-Za-z0-9]+\\s*\\([^)]*[A-Z][A-Za-z0-9_<>?, ]+\\s+[a-z]");
        Pattern cachedDependencyField = Pattern.compile("private\\s+final\\s+(I?Hytale|HttpClient|ObjectMapper).*;");

        for (Path file : files) {
            String source = Files.readString(file);
            assertFalse(source.contains("@Inject"), file + " should not use framework constructor injection.");
            assertFalse(source.contains("@Autowired"), file + " should not use Spring field or constructor injection.");
            assertFalse(injectedConstructor.matcher(source).find(), file + " should not constructor-wire app dependencies.");
            assertFalse(cachedDependencyField.matcher(source).find(), file + " should not cache app dependencies in fields.");
        }
    }

    @Test
    void hytaleDomainDefaultMethodsExposeSingleSurfaceRuntimeTokens() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/tavall/hytale/resourcegame/frontend/hytale/IHytaleFrontendDomainGenerated.java"));

        assertTrue(source.contains("DependencyLoaderAccess.requireInstance(IHytaleFrontendConfig.class)"));
        assertTrue(source.contains("DependencyLoaderAccess.requireInstance(IHytaleControlPlaneCommandBridge.class)"));
        assertTrue(source.contains("DependencyLoaderAccess.requireInstance(IHytaleFrontendModule.class)"));
        assertFalse(source.contains("Proxy"));
        assertFalse(source.contains("Bukkit"));
        assertFalse(source.contains("ServerView"));
    }

    @Test
    void hytaleFrontendDoesNotIntroduceServiceClasses() throws IOException {
        try (var paths = Files.walk(Path.of("src/main/java"))) {
            List<Path> serviceClasses = paths
                    .filter(path -> path.getFileName().toString().endsWith("Service.java"))
                    .toList();

            assertTrue(serviceClasses.isEmpty(), "Hytale frontend should keep Handler naming: " + serviceClasses);
        }
    }
}
