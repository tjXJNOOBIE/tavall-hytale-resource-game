package org.tavall.minecraft.server;

import org.tavall.minecraft.framework.game.ui.UiActions;
import org.tavall.minecraft.framework.game.ui.UiScreenKey;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

final class KingdomInventoryPageCatalog {
    private KingdomInventoryPageCatalog() {
    }

    static KingdomInventoryPageDefinition definition(UiScreenKey pageType, String feedback) {
        return switch (pageType) {
            case CASTLE_MAIN -> new KingdomInventoryPageDefinition(
                    "Kingdom Castle",
                    27,
                    List.of(
                            button(10, Material.BEACON, "Castle Info", UiActions.OPEN_CASTLE_INFO, "", "castle_info.png"),
                            button(11, Material.VILLAGER_SPAWN_EGG, "Citizens", UiActions.OPEN_CITIZENS, "", "castle_citizens.png"),
                            button(12, Material.IRON_SWORD, "Troops", UiActions.OPEN_TROOPS, "", "castle_troops.png"),
                            button(13, Material.EMERALD, "Resources", UiActions.OPEN_RESOURCES, "", "castle_resources.png"),
                            button(14, Material.ANVIL, "Upgrades", UiActions.OPEN_UPGRADES, "", "castle_upgrades.png"),
                            button(15, Material.CRAFTING_TABLE, "Buildings", UiActions.OPEN_BUILDINGS, "", "castle_buildings.png"),
                            button(16, Material.END_PORTAL_FRAME, "Interior", UiActions.ENTER_INTERIOR, "", "castle_main.png"),
                            button(22, Material.BARRIER, "Close", UiActions.CLOSE, "")
                    ),
                    feedback
            );
            case CASTLE_INFO -> new KingdomInventoryPageDefinition(
                    "Castle Info",
                    27,
                    List.of(
                            button(10, Material.BEACON, "Castle", UiActions.OPEN_CASTLE_MAIN, "", "castle_main.png"),
                            button(12, Material.PAPER, "Attack", UiActions.CASTLE_ATTACK_PLACEHOLDER, "", "castle_info.png"),
                            button(14, Material.TRIPWIRE_HOOK, "Friend Access", UiActions.CASTLE_FRIEND_PLACEHOLDER, "", "castle_info.png"),
                            button(16, Material.WRITABLE_BOOK, "Guild Access", UiActions.CASTLE_GUILD_PLACEHOLDER, "", "castle_info.png"),
                            button(22, Material.BARRIER, "Close", UiActions.CLOSE, "")
                    ),
                    feedback
            );
            case CASTLE_CITIZENS -> new KingdomInventoryPageDefinition(
                    "Citizens",
                    27,
                    List.of(
                            button(10, Material.VILLAGER_SPAWN_EGG, "Summary", UiActions.OPEN_CASTLE_MAIN, "", "castle_citizens.png"),
                            button(12, Material.PAPER, "Promote", UiActions.PROMOTE, "", "castle_citizens.png"),
                            button(14, Material.SLIME_BALL, "Demote", UiActions.DEMOTE, "", "castle_citizens.png"),
                            button(16, Material.BOOK, "Upgrades", UiActions.OPEN_UPGRADES, "", "castle_citizens.png"),
                            button(22, Material.BARRIER, "Close", UiActions.CLOSE, "")
                    ),
                    feedback
            );
            case CASTLE_TROOPS -> new KingdomInventoryPageDefinition(
                    "Troops",
                    27,
                    List.of(
                            button(10, Material.IRON_SWORD, "Promote", UiActions.PROMOTE, "", "castle_troops.png"),
                            button(14, Material.REDSTONE, "Demote", UiActions.DEMOTE, "", "castle_troops.png"),
                            button(16, Material.BOOK, "Citizens", UiActions.OPEN_CITIZENS, "", "castle_troops.png"),
                            button(22, Material.BARRIER, "Close", UiActions.CLOSE, "")
                    ),
                    feedback
            );
            case CASTLE_RESOURCES -> new KingdomInventoryPageDefinition(
                    "Resources",
                    27,
                    List.of(
                            button(10, Material.APPLE, "Food", UiActions.DEBUG_NODES_LIST, "", "castle_resources.png"),
                            button(12, Material.OAK_LOG, "Wood", UiActions.DEBUG_NODES_LIST, "", "castle_resources.png"),
                            button(14, Material.IRON_INGOT, "Iron", UiActions.DEBUG_NODES_LIST, "", "castle_resources.png"),
                            button(16, Material.GOLD_INGOT, "Gold", UiActions.DEBUG_NODES_LIST, "", "castle_resources.png"),
                            button(22, Material.BARRIER, "Close", UiActions.CLOSE, "")
                    ),
                    feedback
            );
            case CASTLE_UPGRADES -> new KingdomInventoryPageDefinition(
                    "Upgrades",
                    27,
                    List.of(
                            button(10, Material.ANVIL, "Start Upgrade", UiActions.BUILDING_START_UPGRADE, "", "castle_upgrades.png"),
                            button(12, Material.RED_DYE, "Cancel Upgrade", UiActions.BUILDING_CANCEL_UPGRADE, "", "castle_upgrades.png"),
                            button(14, Material.CRAFTING_TABLE, "Buildings", UiActions.OPEN_BUILDINGS, "", "castle_upgrades.png"),
                            button(16, Material.BOOK, "Castle", UiActions.OPEN_CASTLE_MAIN, "", "castle_upgrades.png"),
                            button(22, Material.BARRIER, "Close", UiActions.CLOSE, "")
                    ),
                    feedback
            );
            case CASTLE_BUILDINGS -> new KingdomInventoryPageDefinition(
                    "Buildings",
                    27,
                    List.of(
                            button(10, Material.CRAFTING_TABLE, "Stage Farmstead", UiActions.BUILDING_STAGE, "farmstead", "castle_buildings.png"),
                            button(11, Material.BRICKS, "Stage Lumber Mill", UiActions.BUILDING_STAGE, "lumber_mill", "castle_buildings.png"),
                            button(12, Material.IRON_BLOCK, "Stage Iron Works", UiActions.BUILDING_STAGE, "iron_works", "castle_buildings.png"),
                            button(14, Material.CAMPFIRE, "Stage Barracks", UiActions.BUILDING_STAGE, "barracks", "castle_buildings.png"),
                            button(15, Material.LOOM, "Stage Workshop", UiActions.BUILDING_STAGE, "workshop", "castle_buildings.png"),
                            button(22, Material.BARRIER, "Close", UiActions.CLOSE, "")
                    ),
                    feedback
            );
            case FARMSTEAD_MENU -> new KingdomInventoryPageDefinition(
                    "Farmstead",
                    27,
                    List.of(
                            button(10, Material.WHEAT, "Place Farmstead", UiActions.BUILDING_PLACE, "farmstead", "farmstead.png"),
                            button(12, Material.ANVIL, "Start Upgrade", UiActions.BUILDING_START_UPGRADE, "", "farmstead.json"),
                            button(14, Material.RED_DYE, "Cancel Upgrade", UiActions.BUILDING_CANCEL_UPGRADE, "", "farmstead.json"),
                            button(16, Material.CRAFTING_TABLE, "Open Buildings", UiActions.OPEN_BUILDINGS, "", "farmstead.png"),
                            button(22, Material.BARRIER, "Close", UiActions.CLOSE, "")
                    ),
                    feedback
            );
            case NPC_MAIN -> new KingdomInventoryPageDefinition(
                    "NPC Interaction",
                    27,
                    List.of(
                            button(10, Material.VILLAGER_SPAWN_EGG, "Overview", UiActions.OPEN_NPC_MAIN, "", "npc_main.png"),
                            button(12, Material.CRAFTING_TABLE, "Open Building", UiActions.NPC_OPEN_BUILDING, "", "npc_main.png"),
                            button(14, Material.WRITABLE_BOOK, "Debug Info", UiActions.NPC_DEBUG, "", true, "NPC debug is available.", "npc_main.json"),
                            button(22, Material.BARRIER, "Close", UiActions.CLOSE, "")
                    ),
                    feedback
            );
            case INTERIOR_MAIN -> new KingdomInventoryPageDefinition(
                    "Interior",
                    27,
                    List.of(
                            button(11, Material.ENDER_EYE, "Enter Interior", UiActions.ENTER_INTERIOR, "", "interior_main.png"),
                            button(15, Material.OAK_DOOR, "Exit Interior", UiActions.EXIT_INTERIOR, "", "interior_main.png"),
                            button(22, Material.BARRIER, "Close", UiActions.CLOSE, "")
                    ),
                    feedback
            );
            case DEBUG_NAVIGATOR -> new KingdomInventoryPageDefinition(
                    "Kingdom Command Center",
                    54,
                    List.of(
                            button(10, Material.BEACON, "Castle", UiActions.OPEN_CASTLE_MAIN, "", "debug_navigator.png"),
                            button(11, Material.BOOK, "Citizens", UiActions.OPEN_CITIZENS, "", "debug_navigator.png"),
                            button(12, Material.IRON_SWORD, "Troops", UiActions.OPEN_TROOPS, "", "debug_navigator.png"),
                            button(13, Material.EMERALD, "Resources", UiActions.OPEN_RESOURCES, "", "debug_navigator.png"),
                            button(14, Material.ANVIL, "Buildings", UiActions.OPEN_BUILDINGS, "", "debug_navigator.png"),
                            button(15, Material.CRAFTING_TABLE, "Interior", UiActions.ENTER_INTERIOR, "", "debug_navigator.png"),
                            button(16, Material.SCAFFOLDING, "Placement", UiActions.OPEN_DEBUG_PLACEMENT, "", "debug_placement.png"),
                            button(19, Material.NETHER_BRICK, "Interior Debug", UiActions.OPEN_DEBUG_INTERIOR, "", "debug_interior.png"),
                            button(20, Material.CHISELED_STONE_BRICKS, "Building Debug", UiActions.OPEN_DEBUG_BUILDINGS, "", "debug_buildings.png"),
                            button(21, Material.MAP, "World Debug", UiActions.OPEN_DEBUG_WORLD, "", "debug_world.png"),
                            button(22, Material.ENDER_EYE, "Scene", UiActions.DEBUG_SCENE_REFRESH, "", "debug_navigator.png"),
                            button(23, Material.WRITABLE_BOOK, "Tutorial", UiActions.DEBUG_TUTORIAL_RESET, "", "debug_navigator.png"),
                            button(40, Material.BARRIER, "Close", UiActions.CLOSE, "", "debug_navigator.png")
                    ),
                    feedback
            );
            case DEBUG_PLACEMENT -> new KingdomInventoryPageDefinition(
                    "Placement",
                    27,
                    List.of(
                            button(10, Material.STONE_BRICKS, "Place Castle", UiActions.PLACEMENT_ARM_CASTLE, "", "debug_placement.png"),
                            button(11, Material.WHEAT_SEEDS, "Food Node", UiActions.PLACEMENT_ARM_NODE, "food", "debug_placement.png"),
                            button(12, Material.OAK_LOG, "Wood Node", UiActions.PLACEMENT_ARM_NODE, "wood", "debug_placement.png"),
                            button(13, Material.IRON_INGOT, "Iron Node", UiActions.PLACEMENT_ARM_NODE, "iron", "debug_placement.png"),
                            button(15, Material.LIME_DYE, "Confirm", UiActions.PLACEMENT_CONFIRM, "", "debug_placement.png"),
                            button(16, Material.RED_DYE, "Cancel", UiActions.PLACEMENT_CANCEL, "", "debug_placement.png"),
                            button(22, Material.BARRIER, "Close", UiActions.CLOSE, "", "debug_placement.png")
                    ),
                    feedback
            );
            case DEBUG_INTERIOR -> new KingdomInventoryPageDefinition(
                    "Interior Debug",
                    27,
                    List.of(
                            button(10, Material.SPAWNER, "Generate", UiActions.DEBUG_INTERIOR_GENERATE, "", "debug_interior.png"),
                            button(11, Material.RESPAWN_ANCHOR, "Rebuild", UiActions.DEBUG_INTERIOR_REBUILD, "", "debug_interior.png"),
                            button(12, Material.BARRIER, "Delete", UiActions.DEBUG_INTERIOR_DELETE, "", "debug_interior.png"),
                            button(13, Material.OAK_PLANKS, "Move", UiActions.DEBUG_INTERIOR_MOVE, "", "debug_interior.png"),
                            button(14, Material.ENDER_PEARL, "Exit", UiActions.DEBUG_INTERIOR_EXIT, "", "debug_interior.png"),
                            button(16, Material.MAP, "Scene Refresh", UiActions.DEBUG_SCENE_REFRESH, "", "debug_interior.png"),
                            button(18, Material.WRITABLE_BOOK, "Tutorial Reset", UiActions.DEBUG_TUTORIAL_RESET, "", "debug_interior.png"),
                            button(22, Material.BARRIER, "Close", UiActions.CLOSE, "", "debug_interior.png")
                    ),
                    feedback
            );
            case DEBUG_BUILDINGS -> new KingdomInventoryPageDefinition(
                    "Building Debug",
                    27,
                    List.of(
                            button(10, Material.BOOKSHELF, "List", UiActions.DEBUG_BUILDINGS_LIST, "", "debug_buildings.png"),
                            button(12, Material.CRAFTING_TABLE, "Focus", UiActions.DEBUG_FOCUS, "", "debug_buildings.png"),
                            button(14, Material.STICK, "Interact", UiActions.DEBUG_INTERACT, "", "debug_buildings.png"),
                            button(22, Material.BARRIER, "Close", UiActions.CLOSE, "", "debug_buildings.png")
                    ),
                    feedback
            );
            case DEBUG_WORLD -> new KingdomInventoryPageDefinition(
                    "World Tools",
                    27,
                    List.of(
                            button(10, Material.MAP, "List Nodes", UiActions.DEBUG_NODES_LIST, "", "debug_world.png"),
                            button(11, Material.BARRIER, "Clear Nodes", UiActions.DEBUG_NODES_CLEAR, "", "debug_world.png"),
                            button(12, Material.CAMPFIRE, "Hologram Test", UiActions.DEBUG_HOLOGRAM_TEST, "", "debug_world.png"),
                            button(14, Material.VILLAGER_SPAWN_EGG, "Spawn Farmer", UiActions.DEBUG_ENTITY_SPAWN, "farmer", "debug_world.png"),
                            button(15, Material.IRON_GOLEM_SPAWN_EGG, "Spawn Miner", UiActions.DEBUG_ENTITY_SPAWN, "miner", "debug_world.png"),
                            button(16, Material.IRON_SWORD, "Spawn Soldier", UiActions.DEBUG_ENTITY_SPAWN, "soldier", "debug_world.png"),
                            button(22, Material.BARRIER, "Close", UiActions.CLOSE, "", "debug_world.png")
                    ),
                    feedback
            );
            case RESOURCE_NODE_DETAIL -> new KingdomInventoryPageDefinition(
                    pageType.name(),
                    27,
                    List.of(
                            button(10, Material.EMERALD, "Assign One", UiActions.NODE_ASSIGN_ONE, "", "resource_node_detail.png"),
                            button(11, Material.EMERALD_BLOCK, "Assign Three", UiActions.NODE_ASSIGN_THREE, "", "resource_node_detail.png"),
                            button(12, Material.DIAMOND, "Assign Five", UiActions.NODE_ASSIGN_FIVE, "", "resource_node_detail.png"),
                            button(13, Material.GOLD_INGOT, "Assign All", UiActions.NODE_ASSIGN_ALL, "", "resource_node_detail.png"),
                            button(14, Material.REDSTONE, "Recall One", UiActions.NODE_RECALL_ONE, "", "resource_node_detail.png"),
                            button(15, Material.REDSTONE_BLOCK, "Recall All", UiActions.NODE_RECALL_ALL, "", "resource_node_detail.png"),
                            button(16, Material.TNT, "Pillage", UiActions.NODE_PILLAGE, "", "resource_node_detail.png"),
                            button(22, Material.BARRIER, "Close", UiActions.CLOSE, "")
                    ),
                    feedback
            );
            case BUILDING_DETAIL -> new KingdomInventoryPageDefinition(
                    "Building Detail",
                    27,
                    List.of(
                            button(10, Material.BEACON, "Overview", UiActions.OPEN_BUILDING_MAIN, "", true, "Open the current building summary.", "building_detail.png"),
                            button(11, Material.CHEST, "Storage", UiActions.OPEN_BUILDING_STORAGE, "", false, "Storage wiring is not modelled yet.", "building_detail.png"),
                            button(12, Material.FURNACE, "Production", UiActions.OPEN_BUILDING_PRODUCTION, "", false, "Production wiring is not modelled yet.", "building_detail.png"),
                            button(13, Material.ANVIL, "Upgrade", UiActions.OPEN_BUILDING_UPGRADE, "", true, "Upgrade the focused building.", "building_detail.png"),
                            button(14, Material.PAPER, "Open Detail", UiActions.BUILDING_OPEN_DETAIL, "", "building_detail.png"),
                            button(15, Material.CRAFTING_TABLE, "Place Building", UiActions.BUILDING_PLACE, "workshop", "building_detail.png"),
                            button(16, Material.RED_DYE, "Back", UiActions.OPEN_BUILDINGS, "", "building_detail.png"),
                            button(22, Material.BARRIER, "Close", UiActions.CLOSE, "")
                    ),
                    feedback
            );
        };
    }

    private static KingdomInventoryButton button(int slot, Material material, String title, String action, String payload) {
        return button(slot, material, title, action, payload, true, "", "");
    }

    private static KingdomInventoryButton button(int slot, Material material, String title, String action, String payload, String assetKey) {
        return button(slot, material, title, action, payload, true, "", assetKey);
    }

    private static KingdomInventoryButton button(int slot, Material material, String title, String action, String payload, boolean enabled, String disabledReason) {
        return button(slot, material, title, action, payload, enabled, disabledReason, "");
    }

    private static KingdomInventoryButton button(int slot, Material material, String title, String action, String payload, boolean enabled, String disabledReason, String assetKey) {
        return new KingdomInventoryButton(slot, material, title, lore(payload, enabled, disabledReason), action, payload, enabled, disabledReason, assetKey);
    }

    private static List<String> lore(String payload, boolean enabled, String disabledReason) {
        ArrayList<String> lore = new ArrayList<String>();
        if (payload != null && !payload.isBlank()) {
            lore.add(payload);
        }
        if (disabledReason != null && !disabledReason.isBlank()) {
            lore.add(disabledReason);
        }
        return List.copyOf(lore);
    }

    record KingdomInventoryPageDefinition(String title, int size, List<KingdomInventoryButton> buttons, String feedback) {
    }

    record KingdomInventoryButton(int slot, Material material, String title, List<String> lore, String action, String payload, boolean enabled, String disabledReason, String assetKey) {
    }
}
