package com.tavall.resourcegame.middleware.cloud;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CloudControlPlaneFilesystemLayoutTest {
    @Test
    void ensureLayoutCreatesRootKingdomAndSupportDirectories() throws IOException {
        Path root = Files.createTempDirectory("cloud-control-plane-layout-test");
        CloudControlPlaneFilesystemLayout layout = new CloudControlPlaneFilesystemLayout(root);

        layout.ensureLayout();
        Path kingdomTwo = layout.ensureKingdom("kingdom-2");

        assertTrue(Files.isDirectory(layout.root()));
        assertTrue(Files.isDirectory(layout.logsDir()));
        assertTrue(Files.isDirectory(layout.kingdomsDir()));
        assertTrue(Files.isDirectory(layout.kingdomDir("kingdom-1")));
        assertTrue(Files.isDirectory(layout.workloadsDir()));
        assertTrue(Files.isDirectory(layout.consolesDir()));
        assertTrue(Files.isDirectory(layout.cacheDir()));
        assertTrue(Files.isDirectory(kingdomTwo));
        assertEquals(List.of("kingdom-1", "kingdom-2"), layout.kingdomIds());
    }
}
