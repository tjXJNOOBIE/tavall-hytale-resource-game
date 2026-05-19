package org.tavall.minecraft.server;

import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.minecraft.framework.game.ui.UiActions;
import org.tavall.minecraft.framework.game.ui.UiScreenKey;
import org.tavall.minecraft.server.commands.support.KingdomCommandSupport;
import org.tavall.minecraft.server.ui.inventory.CommandCenterGuiActionHandler;
import org.tavall.minecraft.server.ui.inventory.CrownboundGuiItemFactory;
import org.tavall.minecraft.server.ui.inventory.GuiButton;
import org.tavall.minecraft.server.ui.inventory.GuiClickContext;
import org.tavall.minecraft.server.ui.inventory.GuiManager;
import org.tavall.minecraft.server.ui.inventory.GuiScreen;
import org.tavall.minecraft.server.ui.inventory.KingdomCommandCenterGuiScreen;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MinecraftBukkitInventoryUiHandler implements IMinecraftBukkitInventoryUiHandler, IBukkitUtilDependencyAccess, IDependencyInjectableConcrete {
    private static final NamespacedKey ACTION_KEY = new NamespacedKey("tavall", "kingdom_action");
    private static final NamespacedKey PAYLOAD_KEY = new NamespacedKey("tavall", "kingdom_payload");
    private static final NamespacedKey BUTTON_FAMILY_KEY = new NamespacedKey("tavall", "kingdom_button_family");
    private static final NamespacedKey KINGDOM_ASSET_KEY = new NamespacedKey("tavall", "kingdom_asset");
    private final GuiManager guiManager = new GuiManager();
    private final CrownboundGuiItemFactory guiItemFactory = new CrownboundGuiItemFactory();
    private final CommandCenterGuiActionHandler commandCenterActions = new CommandCenterGuiActionHandler();

    @Override
    public void open(Player player, UiScreenKey pageType, String feedback) {
        if (pageType == UiScreenKey.DEBUG_NAVIGATOR) {
            Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(MinecraftBukkitServerPlugin.class), () ->
                    guiManager.openGui(player, new KingdomCommandCenterGuiScreen(guiItemFactory, commandCenterActions, feedback))
            );
            return;
        }
        KingdomInventoryPageCatalog.KingdomInventoryPageDefinition definition = KingdomInventoryPageCatalog.definition(pageType, feedback);
        KingdomInventoryUiHolder holder = new KingdomInventoryUiHolder(pageType, feedback);
        Inventory inventory = Bukkit.createInventory(holder, definition.size(), ChatColor.GOLD + definition.title());
        fill(inventory);
        inventory.setItem(4, pageHeader(pageType, definition.title(), feedback));
        for (KingdomInventoryPageCatalog.KingdomInventoryButton button : definition.buttons()) {
            inventory.setItem(button.slot(), button(button.material(), button.title(), button.action(), button.payload(), button.lore(), button.enabled(), button.disabledReason(), button.assetKey(), button.buttonFamily()));
        }
        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(MinecraftBukkitServerPlugin.class), () -> player.openInventory(inventory));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            GuiScreen screen = guiManager.getOpenScreen(player);
            if (screen != null) {
                event.setCancelled(true);
                int slot = event.getRawSlot();
                if (slot < 0 || slot >= screen.size()) {
                    return;
                }
                GuiButton button = screen.getButton(slot);
                if (button == null) {
                    return;
                }
                button.click(new GuiClickContext(player, event, screen, slot));
                return;
            }
        }
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            guiManager.closeGui(player);
        }
    }

    private void handle(Player player, UiScreenKey currentPage, String action, String payload) {
        if (UiActions.CLOSE.equals(action)) {
            player.closeInventory();
            return;
        }
        if (UiActions.RUN_COMMAND.equals(action)) {
            sendCommand(player, payload, "Command sent.");
            return;
        }
        if (UiActions.OPEN_CASTLE_MAIN.equals(action)) {
            open(player, UiScreenKey.CASTLE_MAIN, "Castle overview.");
            return;
        }
        if (UiActions.OPEN_CASTLE_INFO.equals(action)) {
            open(player, UiScreenKey.CASTLE_INFO, "Castle record.");
            return;
        }
        if (UiActions.OPEN_CITIZENS.equals(action)) {
            open(player, UiScreenKey.CASTLE_CITIZENS, "Citizens.");
            return;
        }
        if (UiActions.OPEN_TROOPS.equals(action)) {
            open(player, UiScreenKey.CASTLE_TROOPS, "Troops.");
            return;
        }
        if (UiActions.OPEN_RESOURCES.equals(action)) {
            open(player, UiScreenKey.CASTLE_RESOURCES, "Resources.");
            return;
        }
        if (UiActions.OPEN_UPGRADES.equals(action)) {
            open(player, UiScreenKey.CASTLE_UPGRADES, "Upgrades.");
            return;
        }
        if (UiActions.OPEN_BUILDINGS.equals(action)) {
            open(player, UiScreenKey.CASTLE_BUILDINGS, "Buildings.");
            return;
        }
        if (UiActions.OPEN_NPC_MAIN.equals(action)) {
            open(player, UiScreenKey.NPC_MAIN, "NPC overview.");
            return;
        }
        if (UiActions.OPEN_BUILDING_MAIN.equals(action)) {
            open(player, UiScreenKey.BUILDING_DETAIL, "Building overview.");
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
            open(player, UiScreenKey.DEBUG_NAVIGATOR, "Debug control center.");
            return;
        }
        if (UiActions.OPEN_DEBUG_PLACEMENT.equals(action)) {
            open(player, UiScreenKey.DEBUG_PLACEMENT, "Placement tools.");
            return;
        }
        if (UiActions.OPEN_DEBUG_INTERIOR.equals(action)) {
            open(player, UiScreenKey.DEBUG_INTERIOR, "Interior debug.");
            return;
        }
        if (UiActions.OPEN_DEBUG_BUILDINGS.equals(action)) {
            open(player, UiScreenKey.DEBUG_BUILDINGS, "Building debug.");
            return;
        }
        if (UiActions.OPEN_DEBUG_WORLD.equals(action)) {
            open(player, UiScreenKey.DEBUG_WORLD, "World tools.");
            return;
        }
        if (UiActions.ENTER_INTERIOR.equals(action)) {
            open(player, UiScreenKey.INTERIOR_MAIN, "Interior.");
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
            open(player, UiScreenKey.CASTLE_CITIZENS, "Select a citizen to promote.");
            return;
        }
        if (UiActions.DEMOTE.equals(action)) {
            open(player, UiScreenKey.CASTLE_TROOPS, "Select a troop to demote.");
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
        if (target.isPresent() && target.get().targetType() == org.tavall.api.minecraft.interaction.InteractionTargetType.BUILDING && command.contains("focus")) {
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

    private ItemStack button(Material material, String title, String action, String payload, List<String> lore, boolean enabled, String disabledReason, String assetKey, String buttonFamily) {
        ItemStack item = new ItemStack(material == null ? Material.PAPER : material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(colorForFamily(buttonFamily, enabled) + title);
            ArrayList<String> lines = new ArrayList<String>();
            if (lore != null && !lore.isEmpty()) {
                for (String line : lore) {
                    if (line != null && !line.isBlank()) {
                        lines.add(ChatColor.GRAY + line);
                    }
                }
            }
            if (buttonFamily != null && !buttonFamily.isBlank()) {
                lines.add(ChatColor.DARK_AQUA + "Crownbound button: " + familyLabel(buttonFamily));
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
            NamespacedKey itemModelKey = itemModelForFamily(buttonFamily);
            if (itemModelKey != null) {
                meta.setItemModel(itemModelKey);
            }
            if (buttonFamily != null && !buttonFamily.isBlank()) {
                meta.getPersistentDataContainer().set(BUTTON_FAMILY_KEY, PersistentDataType.STRING, buttonFamily);
            }
            if (assetKey != null && !assetKey.isBlank()) {
                meta.getPersistentDataContainer().set(KINGDOM_ASSET_KEY, PersistentDataType.STRING, assetKey);
            }
            if (payload != null && !payload.isBlank()) {
                meta.getPersistentDataContainer().set(PAYLOAD_KEY, PersistentDataType.STRING, payload);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack pageHeader(UiScreenKey pageType, String title, String feedback) {
        ItemStack item = new ItemStack(headerMaterial(pageType));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + title);
            ArrayList<String> lore = new ArrayList<String>();
            if (feedback != null && !feedback.isBlank()) {
                lore.add(ChatColor.GRAY + feedback);
            }
            if (pageType == UiScreenKey.DEBUG_NAVIGATOR) {
                lore.add(ChatColor.DARK_AQUA + "Crownbound layout: tabs for navigation, primary cards for live actions.");
            }
            for (String line : assetPreview(pageType)) {
                lore.add(ChatColor.DARK_GRAY + line);
            }
            lore.add(ChatColor.DARK_GRAY + "Minecraft assets: " + String.join(", ", pageAssets(pageType)));
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            NamespacedKey headerModelKey = itemModelForPage(pageType);
            if (headerModelKey != null) {
                meta.setItemModel(headerModelKey);
            }
            meta.getPersistentDataContainer().set(KINGDOM_ASSET_KEY, PersistentDataType.STRING, pageType.name().toLowerCase());
            item.setItemMeta(meta);
        }
        return item;
    }

    List<String> assetPreview(UiScreenKey pageType) {
        ArrayList<String> lines = new ArrayList<String>();
        if (pageType == UiScreenKey.CASTLE_MAIN
                || pageType == UiScreenKey.CASTLE_INFO
                || pageType == UiScreenKey.CASTLE_CITIZENS
                || pageType == UiScreenKey.CASTLE_TROOPS
                || pageType == UiScreenKey.CASTLE_RESOURCES
                || pageType == UiScreenKey.CASTLE_UPGRADES
                || pageType == UiScreenKey.CASTLE_BUILDINGS) {
            lines.add("Resource pack root: " + getMinecraftBukkitResourcePackHandler().castleAssetsRoot());
            List<String> expected = getMinecraftBukkitResourcePackHandler().expectedCastleAssetFiles(pageType);
            if (!expected.isEmpty()) {
                lines.add("Expected castle files: " + String.join(", ", expected));
            }
            List<String> files = getMinecraftBukkitResourcePackHandler().castleAssetFiles();
            lines.add(files.isEmpty() ? "Castle assets: none yet." : "Castle assets: " + String.join(", ", files));
            return List.copyOf(lines);
        }
        if (pageType == UiScreenKey.BUILDING_DETAIL || pageType == UiScreenKey.FARMSTEAD_MENU || pageType == UiScreenKey.NPC_MAIN) {
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

    private Material headerMaterial(UiScreenKey pageType) {
        return switch (pageType) {
            case CASTLE_MAIN, CASTLE_INFO, CASTLE_CITIZENS, CASTLE_TROOPS, CASTLE_RESOURCES, CASTLE_UPGRADES, CASTLE_BUILDINGS ->
                    Material.BEACON;
            case FARMSTEAD_MENU, NPC_MAIN, RESOURCE_NODE_DETAIL, BUILDING_DETAIL, INTERIOR_MAIN ->
                    Material.BOOK;
            case DEBUG_NAVIGATOR ->
                    Material.NETHER_STAR;
            case DEBUG_PLACEMENT, DEBUG_INTERIOR, DEBUG_BUILDINGS, DEBUG_WORLD ->
                    Material.MAP;
        };
    }

    private List<String> pageAssets(UiScreenKey pageType) {
        return switch (pageType) {
            case CASTLE_MAIN -> List.of("crownbound:gui/buttons/button_tab", "crownbound:gui/buttons/button_primary", "crownbound:ui/buttons/button_tab.json");
            case CASTLE_INFO -> List.of("crownbound:gui/buttons/button_secondary", "crownbound:gui/buttons/button_icon", "crownbound:ui/buttons/button_secondary.json");
            case CASTLE_CITIZENS -> List.of("crownbound:gui/buttons/button_primary", "crownbound:gui/buttons/button_success", "crownbound:ui/buttons/button_primary.json");
            case CASTLE_TROOPS -> List.of("crownbound:gui/buttons/button_primary", "crownbound:gui/buttons/button_danger", "crownbound:ui/buttons/button_danger.json");
            case CASTLE_RESOURCES -> List.of("crownbound:gui/buttons/button_icon", "crownbound:gui/buttons/button_secondary", "crownbound:ui/buttons/button_icon.json");
            case CASTLE_UPGRADES -> List.of("crownbound:gui/buttons/button_success", "crownbound:gui/buttons/button_danger", "crownbound:ui/buttons/button_success.json");
            case CASTLE_BUILDINGS -> List.of("crownbound:gui/buttons/button_primary", "crownbound:gui/buttons/button_tab", "crownbound:ui/buttons/button_primary.json");
            case FARMSTEAD_MENU -> List.of("crownbound:gui/buttons/button_primary", "crownbound:gui/buttons/button_success", "crownbound:ui/buttons/button_primary.json");
            case NPC_MAIN -> List.of("crownbound:gui/buttons/button_secondary", "crownbound:gui/buttons/button_icon", "crownbound:ui/buttons/button_icon.json");
            case RESOURCE_NODE_DETAIL -> List.of("crownbound:gui/buttons/button_success", "crownbound:gui/buttons/button_danger", "crownbound:ui/buttons/button_success.json");
            case BUILDING_DETAIL -> List.of("crownbound:gui/buttons/button_secondary", "crownbound:gui/buttons/button_success", "crownbound:ui/buttons/button_secondary.json");
            case INTERIOR_MAIN -> List.of("crownbound:gui/buttons/button_primary", "crownbound:gui/buttons/button_danger", "crownbound:ui/buttons/button_primary.json");
            case DEBUG_NAVIGATOR -> List.of(
                    "crownbound:gui/buttons/button_tab",
                    "crownbound:gui/buttons/button_primary",
                    "crownbound:gui/buttons/button_success",
                    "crownbound:gui/buttons/button_icon",
                    "crownbound:gui/buttons/button_danger",
                    "crownbound:ui/buttons/button_families.index.json"
            );
            case DEBUG_PLACEMENT -> List.of("crownbound:gui/buttons/button_success", "crownbound:gui/buttons/button_danger", "crownbound:ui/buttons/button_success.json");
            case DEBUG_INTERIOR -> List.of("crownbound:gui/buttons/button_secondary", "crownbound:gui/buttons/button_icon", "crownbound:ui/buttons/button_secondary.json");
            case DEBUG_BUILDINGS -> List.of("crownbound:gui/buttons/button_secondary", "crownbound:gui/buttons/button_primary", "crownbound:ui/buttons/button_secondary.json");
            case DEBUG_WORLD -> List.of("crownbound:gui/buttons/button_icon", "crownbound:gui/buttons/button_danger", "crownbound:ui/buttons/button_icon.json");
        };
    }

    private ChatColor colorForFamily(String buttonFamily, boolean enabled) {
        if (!enabled) {
            return ChatColor.DARK_RED;
        }
        if (buttonFamily == null || buttonFamily.isBlank()) {
            return ChatColor.GOLD;
        }
        return switch (buttonFamily) {
            case "primary" -> ChatColor.GOLD;
            case "secondary" -> ChatColor.AQUA;
            case "danger" -> ChatColor.RED;
            case "success" -> ChatColor.GREEN;
            case "tab" -> ChatColor.YELLOW;
            case "icon" -> ChatColor.LIGHT_PURPLE;
            default -> ChatColor.GOLD;
        };
    }

    private String familyLabel(String buttonFamily) {
        if (buttonFamily == null || buttonFamily.isBlank()) {
            return "Default";
        }
        return switch (buttonFamily) {
            case "primary" -> "Primary";
            case "secondary" -> "Secondary";
            case "danger" -> "Danger";
            case "success" -> "Success";
            case "tab" -> "Tab";
            case "icon" -> "Icon";
            default -> buttonFamily;
        };
    }

    private NamespacedKey itemModelForFamily(String buttonFamily) {
        if (buttonFamily == null || buttonFamily.isBlank()) {
            return null;
        }
        return switch (buttonFamily) {
            case "primary" -> new NamespacedKey("crownbound", "ui/button_primary");
            case "secondary" -> new NamespacedKey("crownbound", "ui/button_secondary");
            case "danger" -> new NamespacedKey("crownbound", "ui/button_danger");
            case "success" -> new NamespacedKey("crownbound", "ui/button_success");
            case "tab" -> new NamespacedKey("crownbound", "ui/button_tab");
            case "icon" -> new NamespacedKey("crownbound", "ui/button_icon");
            default -> null;
        };
    }

    private NamespacedKey itemModelForPage(UiScreenKey pageType) {
        return switch (pageType) {
            case DEBUG_NAVIGATOR -> new NamespacedKey("crownbound", "ui/button_icon");
            case CASTLE_MAIN, CASTLE_INFO, CASTLE_CITIZENS, CASTLE_TROOPS, CASTLE_RESOURCES, CASTLE_UPGRADES, CASTLE_BUILDINGS ->
                    new NamespacedKey("crownbound", "ui/button_tab");
            case FARMSTEAD_MENU, NPC_MAIN, RESOURCE_NODE_DETAIL, BUILDING_DETAIL, INTERIOR_MAIN ->
                    new NamespacedKey("crownbound", "ui/button_primary");
            case DEBUG_PLACEMENT, DEBUG_INTERIOR, DEBUG_BUILDINGS, DEBUG_WORLD ->
                    new NamespacedKey("crownbound", "ui/button_secondary");
        };
    }
}
