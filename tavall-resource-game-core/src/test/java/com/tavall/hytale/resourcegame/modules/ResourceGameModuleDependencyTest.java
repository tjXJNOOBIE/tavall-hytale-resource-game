package com.tavall.hytale.resourcegame.modules;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ResourceGameModuleDependencyTest {
    private static final Path RESOURCE_GAME_ROOT = resolveResourceGameRoot();
    private static final Pattern VENDORED_ABSTRACT_CACHE_PACKAGE = Pattern.compile(
            "(?m)^\\s*package\\s+org\\.tavall\\.abstractcache"
    );

    @Test
    void frontendModulesUseTavallLoggingAndDiThroughMavenDependencies() throws IOException {
        List<String> frontendModules = List.of(
                "tavall-resource-game-hytale-frontend",
                "tavall-resource-game-minecraft-frontend",
                "tavall-resource-game-roblox-frontend",
                "tavall-resource-game-discord-frontend"
        );

        for (String module : frontendModules) {
            String pom = Files.readString(RESOURCE_GAME_ROOT.resolve(module).resolve("pom.xml"));
            assertTrue(pom.contains("<artifactId>tavall-resource-game-shared-contracts</artifactId>"), module);
            assertTrue(pom.contains("<artifactId>tavall-logging</artifactId>"), module);
            assertTrue(pom.contains("<artifactId>tavall-di</artifactId>"), module);
        }
    }

    @Test
    void controlServerUsesTavallToolingAndCacheThroughMavenDependencies() throws IOException {
        String pom = Files.readString(RESOURCE_GAME_ROOT.resolve("tavall-resource-game-control-server").resolve("pom.xml"));

        assertTrue(pom.contains("<artifactId>tavall-logging</artifactId>"));
        assertTrue(pom.contains("<artifactId>tavall-di</artifactId>"));
        assertTrue(pom.contains("<artifactId>tavall-eventbus</artifactId>"));
        assertTrue(pom.contains("<artifactId>tavall-concurrency</artifactId>"));
        assertTrue(pom.contains("<artifactId>tavall-scheduler</artifactId>"));
        assertTrue(pom.contains("<artifactId>abstract-cache-semantic</artifactId>"));
        assertTrue(pom.contains("<artifactId>abstract-cache-storage-memory</artifactId>"));
    }

    @Test
    void futureAndroidModuleInheritsSharedContractsAndTavallToolsThroughMaven() throws IOException {
        String pom = Files.readString(RESOURCE_GAME_ROOT.resolve("tavall-resource-game-android-app").resolve("pom.xml"));

        assertTrue(pom.contains("<artifactId>tavall-resource-game-shared-contracts</artifactId>"));
        assertTrue(pom.contains("<artifactId>tavall-logging</artifactId>"));
        assertTrue(pom.contains("<artifactId>tavall-di</artifactId>"));
    }

    @Test
    void futurePcModuleTracksSharedContractVersionWithoutClaimingJavaRuntimeConsumption() throws IOException {
        String pom = Files.readString(RESOURCE_GAME_ROOT.resolve("tavall-resource-game-pc-app").resolve("pom.xml"));

        assertTrue(pom.contains("<resource.game.contract.artifactId>tavall-resource-game-shared-contracts</resource.game.contract.artifactId>"));
        assertTrue(pom.contains("<resource.game.future.csharp.contract.namespace>Tavall.ResourceGame.Contracts</resource.game.future.csharp.contract.namespace>"));
    }

    @Test
    void futureAppModulesPointAtFrontendIngressInsteadOfCanonicalDispatchDirectly() throws IOException {
        String androidModule = Files.readString(RESOURCE_GAME_ROOT
                .resolve("tavall-resource-game-android-app")
                .resolve("src/main/kotlin/com/tavall/hytale/resourcegame/android/ResourceGameAndroidModule.kt"));
        String pcModule = Files.readString(RESOURCE_GAME_ROOT
                .resolve("tavall-resource-game-pc-app")
                .resolve("src/ResourceGamePcModule.cs"));

        assertTrue(androidModule.contains("FrontendCommandIngressHandler"));
        assertTrue(pcModule.contains("FrontendCommandIngressHandler"));
    }

    @Test
    void resourceGameRootDoesNotVendAbstractCacheSources() throws IOException {
        try (var paths = Files.walk(RESOURCE_GAME_ROOT)) {
            long vendoredAbstractCacheSourceCount = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.toString().contains("\\target\\"))
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(this::containsVendoredAbstractCachePackage)
                    .count();

            assertEquals(0, vendoredAbstractCacheSourceCount);
        }
    }

    private boolean containsVendoredAbstractCachePackage(Path path) {
        try {
            String source = Files.readString(path);
            return VENDORED_ABSTRACT_CACHE_PACKAGE.matcher(source).find();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to inspect source file " + path, exception);
        }
    }

    /**
     * Maven runs tests from the active module directory, while IDE runs may start from
     * the aggregator root. Resolve both so module dependency tests do not depend on
     * which runner launched them.
     */
    private static Path resolveResourceGameRoot() {
        Path currentDirectory = Path.of("").toAbsolutePath();
        if (Files.exists(currentDirectory.resolve("tavall-resource-game-shared-contracts"))) {
            return currentDirectory;
        }
        Path parentDirectory = currentDirectory.getParent();
        if (parentDirectory != null && Files.exists(parentDirectory.resolve("tavall-resource-game-shared-contracts"))) {
            return parentDirectory;
        }
        throw new IllegalStateException("Unable to resolve Tavall resource game module root from " + currentDirectory);
    }
}
