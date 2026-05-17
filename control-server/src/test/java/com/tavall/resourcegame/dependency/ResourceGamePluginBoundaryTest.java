package com.tavall.resourcegame.dependency;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ResourceGamePluginBoundaryTest {
    @Test
    void corePluginDelegatesLifecycleToBootstrapOnly() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/ResourceGamePlugin.java"));

        assertTrue(source.contains("bootstrap.setup(this)"));
        assertTrue(source.contains("bootstrap.start(this)"));
        assertTrue(source.contains("bootstrap.shutdown(this)"));
        assertFalse(source.contains("getEventRegistry().registerGlobal(PlayerReadyEvent.class"));
        assertFalse(source.contains("getDebugCommandService().commands()"));
        assertFalse(source.contains("AsyncTask.shutdown()"));
    }
}
