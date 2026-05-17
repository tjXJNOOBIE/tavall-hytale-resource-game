package com.tavall.resourcegame.frontend.minecraft.server;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class MinecraftBukkitResourcePackHandlerTest {
    @Test
    void ensureLayoutCreatesCastleAndBuildingFolders() throws IOException {
        Path root = Files.createTempDirectory("tavall-resource-pack");
        Path configuredRoot = root.resolve("resource-pack");
        MinecraftBukkitResourcePackHandler handler = new MinecraftBukkitResourcePackHandler(configuredRoot);

        handler.ensureLayout();

        assertTrue(Files.isDirectory(configuredRoot));
        assertTrue(Files.isDirectory(configuredRoot.resolve("castles")));
        assertTrue(Files.isDirectory(configuredRoot.resolve("buildings")));
        assertTrue(handler.statusLine().contains("castles="));
        assertTrue(handler.statusLine().contains("buildings="));
    }
}
