package org.tavall.minecraft.server;

import org.tavall.minecraft.framework.game.ui.UiActions;
import org.tavall.minecraft.framework.game.ui.UiScreenKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class KingdomInventoryPageCatalogTest {
    @Test
    void castleMainPageExposesTheExpectedInventoryAssets() {
        KingdomInventoryPageCatalog.KingdomInventoryPageDefinition definition = KingdomInventoryPageCatalog.definition(UiScreenKey.CASTLE_MAIN, "Ready.");

        assertEquals("Kingdom Castle", definition.title());
        assertEquals(27, definition.size());
        assertTrue(definition.buttons().stream().anyMatch(button -> UiActions.OPEN_CITIZENS.equals(button.action())));
        assertTrue(definition.buttons().stream().anyMatch(button -> UiActions.OPEN_RESOURCES.equals(button.action())));
        assertTrue(definition.buttons().stream().anyMatch(button -> UiActions.CLOSE.equals(button.action())));
        assertTrue(definition.buttons().stream().anyMatch(button -> "castle_main.png".equals(button.assetKey())));
        assertTrue(definition.buttons().stream().anyMatch(button -> "castle_buildings.png".equals(button.assetKey())));
    }

    @Test
    void commandCenterPageExposesTheAdminHubLayout() {
        KingdomInventoryPageCatalog.KingdomInventoryPageDefinition definition = KingdomInventoryPageCatalog.definition(UiScreenKey.DEBUG_NAVIGATOR, "Ready.");

        assertEquals("Kingdom Command Center", definition.title());
        assertEquals(45, definition.size());
        assertTrue(definition.buttons().stream().anyMatch(button -> UiActions.OPEN_CASTLE_MAIN.equals(button.action())));
        assertTrue(definition.buttons().stream().anyMatch(button -> UiActions.OPEN_CITIZENS.equals(button.action())));
        assertTrue(definition.buttons().stream().anyMatch(button -> UiActions.RUN_COMMAND.equals(button.action()) && "kd ui account".equals(button.payload())));
        assertTrue(definition.buttons().stream().anyMatch(button -> UiActions.RUN_COMMAND.equals(button.action()) && "kd ui debug".equals(button.payload())));
        assertTrue(definition.buttons().stream().anyMatch(button -> "tab".equals(button.buttonFamily())));
        assertTrue(definition.buttons().stream().anyMatch(button -> "primary".equals(button.buttonFamily())));
        assertTrue(definition.buttons().stream().anyMatch(button -> "danger".equals(button.buttonFamily())));
    }

    @Test
    void farmsteadAndNodePagesExposeTheNewInventoryFlows() {
        KingdomInventoryPageCatalog.KingdomInventoryPageDefinition farmstead = KingdomInventoryPageCatalog.definition(UiScreenKey.FARMSTEAD_MENU, "Ready.");
        KingdomInventoryPageCatalog.KingdomInventoryPageDefinition node = KingdomInventoryPageCatalog.definition(UiScreenKey.RESOURCE_NODE_DETAIL, "Ready.");
        KingdomInventoryPageCatalog.KingdomInventoryPageDefinition npc = KingdomInventoryPageCatalog.definition(UiScreenKey.NPC_MAIN, "Ready.");
        KingdomInventoryPageCatalog.KingdomInventoryPageDefinition building = KingdomInventoryPageCatalog.definition(UiScreenKey.BUILDING_DETAIL, "Ready.");
        KingdomInventoryPageCatalog.KingdomInventoryPageDefinition debugNavigator = KingdomInventoryPageCatalog.definition(UiScreenKey.DEBUG_NAVIGATOR, "Ready.");
        KingdomInventoryPageCatalog.KingdomInventoryPageDefinition debugPlacement = KingdomInventoryPageCatalog.definition(UiScreenKey.DEBUG_PLACEMENT, "Ready.");
        KingdomInventoryPageCatalog.KingdomInventoryPageDefinition debugInterior = KingdomInventoryPageCatalog.definition(UiScreenKey.DEBUG_INTERIOR, "Ready.");
        KingdomInventoryPageCatalog.KingdomInventoryPageDefinition debugBuildings = KingdomInventoryPageCatalog.definition(UiScreenKey.DEBUG_BUILDINGS, "Ready.");
        KingdomInventoryPageCatalog.KingdomInventoryPageDefinition debugWorld = KingdomInventoryPageCatalog.definition(UiScreenKey.DEBUG_WORLD, "Ready.");

        assertTrue(farmstead.buttons().stream().anyMatch(button -> UiActions.BUILDING_PLACE.equals(button.action())));
        assertTrue(farmstead.buttons().stream().anyMatch(button -> UiActions.BUILDING_START_UPGRADE.equals(button.action())));
        assertTrue(npc.buttons().stream().anyMatch(button -> UiActions.NPC_OPEN_BUILDING.equals(button.action())));
        assertTrue(building.buttons().stream().anyMatch(button -> UiActions.OPEN_BUILDING_MAIN.equals(button.action())));
        assertTrue(node.buttons().stream().anyMatch(button -> UiActions.NODE_ASSIGN_ALL.equals(button.action())));
        assertTrue(node.buttons().stream().anyMatch(button -> UiActions.NODE_PILLAGE.equals(button.action())));
        assertTrue(farmstead.buttons().stream().anyMatch(button -> "farmstead.png".equals(button.assetKey())));
        assertTrue(node.buttons().stream().anyMatch(button -> "resource_node_detail.png".equals(button.assetKey())));
        assertTrue(building.buttons().stream().anyMatch(button -> "building_detail.png".equals(button.assetKey())));
        assertTrue(debugNavigator.buttons().stream().anyMatch(button -> "debug_navigator.png".equals(button.assetKey())));
        assertTrue(debugPlacement.buttons().stream().anyMatch(button -> "debug_placement.png".equals(button.assetKey())));
        assertTrue(debugInterior.buttons().stream().anyMatch(button -> "debug_interior.png".equals(button.assetKey())));
        assertTrue(debugBuildings.buttons().stream().anyMatch(button -> "debug_buildings.png".equals(button.assetKey())));
        assertTrue(debugWorld.buttons().stream().anyMatch(button -> "debug_world.png".equals(button.assetKey())));
    }
}
