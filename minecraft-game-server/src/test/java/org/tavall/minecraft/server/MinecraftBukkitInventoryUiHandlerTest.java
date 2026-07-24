package org.tavall.minecraft.server;

import org.tavall.dependency.DependencyLoader;
import org.tavall.dependency.DependencyLoaderAccess;
import org.tavall.minecraft.framework.game.ui.UiScreenKey;
import org.tavall.minecraft.server.resourcepack.IMinecraftBukkitResourcePackHandler;
import org.tavall.minecraft.server.resourcepack.MinecraftBukkitResourcePackHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class MinecraftBukkitInventoryUiHandlerTest {
    @Test
    void castleAndBuildingPagesAdvertiseResourcePackRoots() throws IOException {
        DependencyLoader.getDependencyLoader().clear();
        org.tavall.dependency.DependencyLoaderAccess.clear();
        Path root = Files.createTempDirectory("tavall-resource-pack-ui");
        Files.createDirectories(root.resolve("castles"));
        Files.createDirectories(root.resolve("buildings"));
        Files.writeString(root.resolve("castles").resolve("castle_keep.png"), "");
        Files.writeString(root.resolve("buildings").resolve("farmstead.png"), "");
        DependencyLoaderAccess.registerInstance(IMinecraftBukkitServerConfig.class, new MinecraftBukkitServerConfig("kingdom", "proxy", 200L, root.toString()));
        DependencyLoaderAccess.registerInstance(IMinecraftBukkitResourcePackHandler.class, MinecraftBukkitResourcePackHandler.forRoot(root));

        MinecraftBukkitInventoryUiHandler handler = new MinecraftBukkitInventoryUiHandler();

        assertTrue(handler.assetPreview(UiScreenKey.CASTLE_MAIN).stream().anyMatch(line -> line.contains("castles")));
        assertTrue(handler.assetPreview(UiScreenKey.CASTLE_MAIN).stream().anyMatch(line -> line.contains("castle_main.png")));
        assertTrue(handler.assetPreview(UiScreenKey.CASTLE_MAIN).stream().anyMatch(line -> line.contains("castle_keep.png")));
        assertTrue(handler.assetPreview(UiScreenKey.BUILDING_DETAIL).stream().anyMatch(line -> line.contains("buildings")));
        assertTrue(handler.assetPreview(UiScreenKey.BUILDING_DETAIL).stream().anyMatch(line -> line.contains("building_detail.png")));
        assertTrue(handler.assetPreview(UiScreenKey.BUILDING_DETAIL).stream().anyMatch(line -> line.contains("farmstead.png")));
        assertTrue(handler.assetPreview(UiScreenKey.DEBUG_NAVIGATOR).isEmpty());
    }
}
