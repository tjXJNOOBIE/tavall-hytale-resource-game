package com.tavall.resourcegame.frontend.minecraft.server;

import com.tjxjnoobie.api.dependency.DependencyLoader;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.api.internal.ui.UiPageType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class MinecraftBukkitInventoryUiHandlerTest {
    @Test
    void castleAndBuildingPagesAdvertiseResourcePackRoots() throws IOException {
        DependencyLoader.getDependencyLoader().clear();
        com.tjxjnoobie.api.dependency.DependencyLoaderAccess.clear();
        Path root = Files.createTempDirectory("tavall-resource-pack-ui");
        Files.createDirectories(root.resolve("castles"));
        Files.createDirectories(root.resolve("buildings"));
        Files.writeString(root.resolve("castles").resolve("castle_keep.png"), "");
        Files.writeString(root.resolve("buildings").resolve("farmstead.png"), "");
        DependencyLoaderAccess.registerInstance(IMinecraftBukkitServerConfig.class, new MinecraftBukkitServerConfig("kingdom", "proxy", 200L, root.toString()));
        DependencyLoaderAccess.registerInstance(IMinecraftBukkitResourcePackHandler.class, new MinecraftBukkitResourcePackHandler(root));

        MinecraftBukkitInventoryUiHandler handler = new MinecraftBukkitInventoryUiHandler();

        assertTrue(handler.assetPreview(UiPageType.CASTLE_MAIN).stream().anyMatch(line -> line.contains("castles")));
        assertTrue(handler.assetPreview(UiPageType.CASTLE_MAIN).stream().anyMatch(line -> line.contains("castle_main.png")));
        assertTrue(handler.assetPreview(UiPageType.CASTLE_MAIN).stream().anyMatch(line -> line.contains("castle_keep.png")));
        assertTrue(handler.assetPreview(UiPageType.BUILDING_DETAIL).stream().anyMatch(line -> line.contains("buildings")));
        assertTrue(handler.assetPreview(UiPageType.BUILDING_DETAIL).stream().anyMatch(line -> line.contains("building_detail.png")));
        assertTrue(handler.assetPreview(UiPageType.BUILDING_DETAIL).stream().anyMatch(line -> line.contains("farmstead.png")));
        assertTrue(handler.assetPreview(UiPageType.DEBUG_NAVIGATOR).isEmpty());
    }
}
