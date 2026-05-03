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
    private static final Path RESOURCE_GAME_ROOT = Path.of("").toAbsolutePath();
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
}
