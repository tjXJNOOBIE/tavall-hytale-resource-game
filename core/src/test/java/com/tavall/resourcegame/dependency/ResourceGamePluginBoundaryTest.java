package com.tavall.resourcegame.dependency;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ResourceGamePluginBoundaryTest {
    @Test
    void corePluginRegistersGameplayEventsAndDebugCommandsOnly() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/ResourceGamePlugin.java"));

        assertTrue(source.contains("getEventRegistry().registerGlobal(PlayerReadyEvent.class, getPlayerDataService()::handlePlayerReady)"));
        assertTrue(source.contains("getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, getPlayerDataService()::handlePlayerDisconnect)"));
        assertTrue(source.contains("getEventRegistry().registerGlobal(PlayerInteractEvent.class, getPlacementInteractionService()::handleInteract)"));
        assertTrue(source.contains("getEventRegistry().registerGlobal(PlayerInteractEvent.class, getCastleInteractionService()::handleInteract)"));
        assertTrue(source.contains("getEventRegistry().registerGlobal(PlayerInteractEvent.class, getResourceNodeInteractionService()::handleInteract)"));
        assertTrue(source.contains("getEventRegistry().registerGlobal(PlayerInteractEvent.class, getBuildingInteractionService()::handleInteract)"));
        assertTrue(source.contains("getEventRegistry().registerGlobal(PlayerInteractEvent.class, getWorkerNpcInteractionService()::handleInteract)"));
        assertTrue(source.contains("getEventRegistry().registerGlobal(PlayerInteractEvent.class, getCustomEntitySpawnService()::handleInteract)"));
        assertTrue(source.contains("getDebugCommandService().commands()"));
        assertFalse(source.contains("metaBuilder(\"rank\")"));
        assertFalse(source.contains("PluginCommand"));
        assertFalse(source.contains("ProxyServer"));
    }
}
