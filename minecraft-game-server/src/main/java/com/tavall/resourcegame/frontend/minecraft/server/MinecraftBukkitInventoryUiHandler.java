package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.api.internal.frontend.FrontendCommandVerificationResult;
import com.tavall.resourcegame.api.internal.ui.UiActions;
import com.tavall.resourcegame.api.internal.ui.UiPageType;
import com.tavall.resourcegame.frontend.minecraft.server.commands.util.KingdomCommandSupport;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MinecraftBukkitInventoryUiHandler implements IMinecraftBukkitInventoryUiHandler, IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    private static final NamespacedKey ACTION_KEY = new NamespacedKey("tavall", "kingdom_action");
    private static final NamespacedKey PAYLOAD_KEY = new NamespacedKey("tavall", "kingdom_payload");

    @Override
    public void open(Player player, UiPageType pageType, String feedback) {
        KingdomInventoryPageCatalog.KingdomInventoryPageDefinition definition = KingdomInventoryPageCatalog.definition(pageType, feedback);
        KingdomInventoryUiHolder holder = new KingdomInventoryUiHolder(pageType, feedback);
        Inventory inventory = Bukkit.createInventory(holder, definition.size(), ChatColor.GOLD + definition.title());
        fill(inventory);
        inventory.setItem(4, pageHeader(pageType, definition.title(), feedback));
        for (KingdomInventoryPageCatalog.KingdomInventoryButton button : definition.buttons()) {
            inventory.setItem(button.slot(), button(button.material(), button.title(), button.action(), button.payload(), button.lore(), button.enabled(), button.disabledReason(), button.assetKey()));
        }
        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(MinecraftBukkitServerPlugin.class), () -> player.openInventory(inventory));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof KingdomInventoryUiHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || !currentItem.hasItemMeta()) {
            return;
        }
        ItemMeta meta = currentItem.getItemMeta();
        String action = meta.getPersistentDataContainer().get(ACTION_KEY, PersistentDataType.STRING);
        String payload = meta.getPersistentDataContainer().get(PAYLOAD_KEY, PersistentDataType.STRING);
        if (action == null || action.isBlank()) {
            return;
        }
        handle(player, holder.pageType(), action, payload == null ? "" : payload);
    }

    private void handle(Player player, UiPageType currentPage, String action, String payload) {
        if (UiActions.CLOSE.equals(action)) {
            player.closeInventory();
            return;
        }
        if (UiActions.OPEN_CASTLE_MAIN.equals(action)) {
            open(player, UiPageType.CASTLE_MAIN, "Castle overview.");
            return;
        }
        if (UiActions.OPEN_CASTLE_INFO.equals(action)) {
            open(player, UiPageType.CASTLE_INFO, "Castle record.");
            return;
        }
        if (UiActions.OPEN_CITIZENS.equals(action)) {
            open(player, UiPageType.CASTLE_CITIZENS, "Citizens.");
            return;
        }
        if (UiActions.OPEN_TROOPS.equals(action)) {
            open(player, UiPageType.CASTLE_TROOPS, "Troops.");
            return;
        }
        if (UiActions.OPEN_RESOURCES.equals(action)) {
            open(player, UiPageType.CASTLE_RESOURCES, "Resources.");
            return;
        }
        if (UiActions.OPEN_UPGRADES.equals(action)) {
            open(player, UiPageType.CASTLE_UPGRADES, "Upgrades.");
            return;
        }
        if (UiActions.OPEN_BUILDINGS.equals(action)) {
            open(player, UiPageType.CASTLE_BUILDINGS, "Buildings.");
            return;
        }
        if (UiActions.OPEN_NPC_MAIN.equals(action)) {
            open(player, UiPageType.NPC_MAIN, "NPC overview.");
            return;
        }
        if (UiActions.OPEN_BUILDING_MAIN.equals(action)) {
            open(player, UiPageType.BUILDING_DETAIL, "Building overview.");
            return;
        }
        if (UiActions.OPEN_BUILDING_STORAGE.equals(action)) {
            sendCommand(player, actionCommand(player, "kd buildings status focus", payload), "Building storage opened.");
            return;
        }
        if (UiActions.OPEN_BUILDING_PRODUCTION.equals(action)) {
            sendCommand(player, actionCommand(player, "kd buildings status focus", payload), "Building production opened.");
            return;
        }
        if (UiActions.OPEN_BUILDING_UPGRADE.equals(action)) {
            sendCommand(player, actionCommand(player, "kd buildings upgrade focus", payload), "Building upgrade requested.");
            return;
        }
        if (UiActions.NPC_OPEN_BUILDING.equals(action)) {
            sendCommand(player, actionCommand(player, "kd buildings select focus", payload), "Focused building opened.");
            return;
        }
        if (UiActions.NPC_DEBUG.equals(action)) {
            sendCommand(player, "kd citizens summary", "NPC summary opened.");
            return;
        }
        if (UiActions.OPEN_DEBUG.equals(action)) {
            open(player, UiPageType.DEBUG_NAVIGATOR, "Debug control center.");
            return;
        }
        if (UiActions.OPEN_DEBUG_PLACEMENT.equals(action)) {
            open(player, UiPageType.DEBUG_PLACEMENT, "Placement tools.");
            return;
        }
        if (UiActions.OPEN_DEBUG_INTERIOR.equals(action)) {
            open(player, UiPageType.DEBUG_INTERIOR, "Interior debug.");
            return;
        }
        if (UiActions.OPEN_DEBUG_BUILDINGS.equals(action)) {
            open(player, UiPageType.DEBUG_BUILDINGS, "Building debug.");
            return;
        }
        if (UiActions.OPEN_DEBUG_WORLD.equals(action)) {
            open(player, UiPageType.DEBUG_WORLD, "World tools.");
            return;
        }
        if (UiActions.ENTER_INTERIOR.equals(action)) {
            open(player, UiPageType.INTERIOR_MAIN, "Interior.");
            return;
        }
        if (UiActions.EXIT_INTERIOR.equals(action)) {
            sendCommand(player, "kd interior exit", "Interior exit sent.");
            return;
        }
        if (UiActions.DEBUG_NODES_LIST.equals(action)) {
            sendCommand(player, "kd nodes list", "Node listing sent.");
            return;
        }
        if (UiActions.DEBUG_NODES_CLEAR.equals(action)) {
            sendCommand(player, "kd nodes clear", "Node clearing sent.");
            return;
        }
        if (UiActions.DEBUG_HOLOGRAM_TEST.equals(action)) {
            sendCommand(player, "kd hologram spawn kingdom-debug", "Hologram command sent.");
            return;
        }
        if (UiActions.DEBUG_ENTITY_CLEAR.equals(action)) {
            sendCommand(player, "kd entity clear", "Entity clearing sent.");
            return;
        }
        if (UiActions.DEBUG_ENTITY_SPAWN.equals(action)) {
            sendCommand(player, joinCommand("kd entity spawn", payload), "Entity spawn sent.");
            return;
        }
        if (UiActions.BUILDING_STAGE.equals(action)) {
            sendCommand(player, joinCommand("kd buildings stage", payload), "Building staging requested.");
            return;
        }
        if (UiActions.BUILDING_OPEN_DETAIL.equals(action)) {
            sendCommand(player, actionCommand(player, "kd buildings select focus", payload), "Building detail opened.");
            return;
        }
        if (UiActions.PLACEMENT_ARM_CASTLE.equals(action)) {
            sendCommand(player, "kd place castle", "Castle placement armed.");
            return;
        }
        if (UiActions.PLACEMENT_ARM_NODE.equals(action)) {
            sendCommand(player, joinCommand("kd place node", payload), "Node placement armed.");
            return;
        }
        if (UiActions.PLACEMENT_CONFIRM.equals(action)) {
            sendCommand(player, "kd place confirm", "Placement confirmation sent.");
            return;
        }
        if (UiActions.PLACEMENT_CANCEL.equals(action)) {
            sendCommand(player, "kd place cancel", "Placement cancel sent.");
            return;
        }
        if (UiActions.PROMOTE.equals(action)) {
            open(player, UiPageType.CASTLE_CITIZENS, "Select a citizen to promote.");
            return;
        }
        if (UiActions.DEMOTE.equals(action)) {
            open(player, UiPageType.CASTLE_TROOPS, "Select a troop to demote.");
            return;
        }
        if (UiActions.CLOSE.equals(action)) {
            player.closeInventory();
            return;
        }
        player.sendMessage(ChatColor.YELLOW + "No action bound for " + currentPage.name() + ": " + action);
    }

    private void sendCommand(Player player, String rawCommand, String feedback) {
        try {
            FrontendCommandVerificationResult result = getMinecraftBukkitCommandClientHandler().submitCommand(
                    player.getUniqueId().toString(),
                    player.getName(),
                    rawCommand,
                    "minecraft-bukkit-ui-" + java.util.UUID.randomUUID(),
                    KingdomCommandSupport.commandMetadata(this, player, "kd")
            );
            KingdomCommandSupport.renderFeedback(this, player, rawCommand, result);
        } catch (Exception exception) {
            player.sendMessage(ChatColor.RED + feedback + " " + exception.getMessage());
        }
    }

    private String actionCommand(Player player, String command, String payload) {
        Optional<MinecraftBukkitInteractionTarget> target = getMinecraftBukkitInteractionSessionTracker().current(player.getUniqueId());
        if (target.isPresent() && target.get().targetType() == com.tavall.resourcegame.api.internal.interaction.InteractionTargetType.BUILDING && command.contains("focus")) {
            return command.replace("focus", target.get().targetId());
        }
        return joinCommand(command, payload);
    }

    private String joinCommand(String base, String payload) {
        if (payload == null || payload.isBlank()) {
            return base;
        }
        return base + " " + payload.trim();
    }

    private void fill(Inventory inventory) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
    }

    private ItemStack button(Material material, String title, String action, String payload, List<String> lore, boolean enabled, String disabledReason, String assetKey) {
        ItemStack item = new ItemStack(material == null ? Material.PAPER : material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName((enabled ? ChatColor.GOLD : ChatColor.DARK_RED) + title);
            ArrayList<String> lines = new ArrayList<String>();
            if (lore != null && !lore.isEmpty()) {
                for (String line : lore) {
                    if (line != null && !line.isBlank()) {
                        lines.add(ChatColor.GRAY + line);
                    }
                }
            }
            if (!enabled && disabledReason != null && !disabledReason.isBlank()) {
                lines.add(ChatColor.RED + disabledReason);
            }
            if (assetKey != null && !assetKey.isBlank()) {
                lines.add(ChatColor.DARK_GRAY + "asset: " + assetKey);
            }
            if (payload != null && !payload.isBlank()) {
                lines.add(ChatColor.DARK_GRAY + "payload: " + payload);
            }
            meta.setLore(lines);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            if (enabled) {
                meta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, action);
            }
            if (assetKey != null && !assetKey.isBlank()) {
                meta.getPersistentDataContainer().set(new NamespacedKey("tavall", "kingdom_asset"), PersistentDataType.STRING, assetKey);
            }
            if (payload != null && !payload.isBlank()) {
                meta.getPersistentDataContainer().set(PAYLOAD_KEY, PersistentDataType.STRING, payload);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack pageHeader(UiPageType pageType, String title, String feedback) {
        ItemStack item = new ItemStack(headerMaterial(pageType));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + title);
            ArrayList<String> lore = new ArrayList<String>();
            if (feedback != null && !feedback.isBlank()) {
                lore.add(ChatColor.GRAY + feedback);
            }
            for (String line : assetPreview(pageType)) {
                lore.add(ChatColor.DARK_GRAY + line);
            }
            lore.add(ChatColor.DARK_GRAY + "Hytale assets: " + String.join(", ", pageAssets(pageType)));
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    List<String> assetPreview(UiPageType pageType) {
        ArrayList<String> lines = new ArrayList<String>();
        if (pageType == UiPageType.CASTLE_MAIN
                || pageType == UiPageType.CASTLE_INFO
                || pageType == UiPageType.CASTLE_CITIZENS
                || pageType == UiPageType.CASTLE_TROOPS
                || pageType == UiPageType.CASTLE_RESOURCES
                || pageType == UiPageType.CASTLE_UPGRADES
                || pageType == UiPageType.CASTLE_BUILDINGS) {
            lines.add("Resource pack root: " + getMinecraftBukkitResourcePackHandler().castleAssetsRoot());
            List<String> expected = getMinecraftBukkitResourcePackHandler().expectedCastleAssetFiles(pageType);
            if (!expected.isEmpty()) {
                lines.add("Expected castle files: " + String.join(", ", expected));
            }
            List<String> files = getMinecraftBukkitResourcePackHandler().castleAssetFiles();
            lines.add(files.isEmpty() ? "Castle assets: none yet." : "Castle assets: " + String.join(", ", files));
            return List.copyOf(lines);
        }
        if (pageType == UiPageType.BUILDING_DETAIL || pageType == UiPageType.FARMSTEAD_MENU || pageType == UiPageType.NPC_MAIN) {
            lines.add("Resource pack root: " + getMinecraftBukkitResourcePackHandler().buildingAssetsRoot());
            List<String> expected = getMinecraftBukkitResourcePackHandler().expectedBuildingAssetFiles(pageType);
            if (!expected.isEmpty()) {
                lines.add("Expected building files: " + String.join(", ", expected));
            }
            List<String> files = getMinecraftBukkitResourcePackHandler().buildingAssetFiles();
            lines.add(files.isEmpty() ? "Building assets: none yet." : "Building assets: " + String.join(", ", files));
            return List.copyOf(lines);
        }
        return List.of();
    }

    private Material headerMaterial(UiPageType pageType) {
        return switch (pageType) {
            case CASTLE_MAIN, CASTLE_INFO, CASTLE_CITIZENS, CASTLE_TROOPS, CASTLE_RESOURCES, CASTLE_UPGRADES, CASTLE_BUILDINGS ->
                    Material.BEACON;
            case FARMSTEAD_MENU, NPC_MAIN, RESOURCE_NODE_DETAIL, BUILDING_DETAIL, INTERIOR_MAIN ->
                    Material.BOOK;
            case DEBUG_NAVIGATOR, DEBUG_PLACEMENT, DEBUG_INTERIOR, DEBUG_BUILDINGS, DEBUG_WORLD ->
                    Material.MAP;
        };
    }

    private List<String> pageAssets(UiPageType pageType) {
        return switch (pageType) {
            case CASTLE_MAIN -> List.of("ui_panel_castle_ledger_base", "ui_icon_kingdom_castle", "ui_divider_section_gold");
            case CASTLE_INFO -> List.of("ui_panel_castle_ledger_base", "ui_badge_status_blocked");
            case CASTLE_CITIZENS -> List.of("ui_panel_castle_ledger_base", "ui_icon_population_worker");
            case CASTLE_TROOPS -> List.of("ui_panel_war_table_base", "ui_icon_population_troop");
            case CASTLE_RESOURCES -> List.of("ui_panel_castle_ledger_base", "ui_icon_resource_food", "ui_icon_resource_wood", "ui_icon_resource_iron", "ui_icon_resource_gold");
            case CASTLE_UPGRADES -> List.of("ui_panel_workshop_base", "ui_icon_action_upgrade");
            case CASTLE_BUILDINGS -> List.of("ui_panel_workshop_base", "ui_icon_building_farmstead", "ui_icon_building_lumber_mill", "ui_icon_building_iron_works", "ui_icon_building_barracks", "ui_icon_building_workshop");
            case FARMSTEAD_MENU -> List.of("ui_panel_workshop_base", "ui_icon_building_farmstead");
            case NPC_MAIN -> List.of("ui_panel_npc_detail_base", "ui_icon_population_worker", "ui_icon_action_info");
            case RESOURCE_NODE_DETAIL -> List.of("ui_panel_node_detail_base", "ui_icon_node_marker");
            case BUILDING_DETAIL -> List.of("ui_panel_workshop_base", "ui_icon_action_move", "ui_icon_action_upgrade", "ui_icon_action_storage");
            case INTERIOR_MAIN -> List.of("ui_panel_interior_base", "ui_icon_action_move");
            case DEBUG_NAVIGATOR -> List.of("ui_panel_war_table_base", "ui_badge_status_progress");
            case DEBUG_PLACEMENT -> List.of("ui_selector_building_valid", "ui_selector_corner_valid", "ui_selector_radius_ring");
            case DEBUG_INTERIOR -> List.of("ui_panel_interior_base", "ui_badge_status_available");
            case DEBUG_BUILDINGS -> List.of("ui_panel_workshop_base", "ui_icon_action_move");
            case DEBUG_WORLD -> List.of("ui_panel_castle_ledger_base", "ui_icon_action_blocked", "ui_icon_node_marker");
        };
    }
}
