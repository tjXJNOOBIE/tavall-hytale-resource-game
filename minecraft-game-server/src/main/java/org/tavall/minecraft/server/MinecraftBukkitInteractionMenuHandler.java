package org.tavall.minecraft.server;

import org.tavall.api.minecraft.interaction.InteractionMenuElement;
import org.tavall.api.minecraft.interaction.InteractionMenuModel;
import org.tavall.api.minecraft.interaction.InteractionRequest;
import org.tavall.api.minecraft.interaction.InteractionResult;
import org.tavall.api.minecraft.interaction.InteractionResultType;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendSurfaceIdentity;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MinecraftBukkitInteractionMenuHandler implements IMinecraftBukkitInteractionMenuHandler, IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    private static final NamespacedKey ACTION_KEY = new NamespacedKey("tavall", "interaction_action");
    private static final NamespacedKey ELEMENT_KEY = new NamespacedKey("tavall", "interaction_element");
    private static final NamespacedKey PAYLOAD_KEY = new NamespacedKey("tavall", "interaction_payload");

    @Override
    public void open(Player player, InteractionMenuModel menu, String feedback) {
        MinecraftBukkitInteractionTarget target = new MinecraftBukkitInteractionTarget(
                menu.targetType(),
                menu.targetId(),
                menu.title(),
                menu.metadata()
        );
        MinecraftBukkitInteractionMenuHolder holder = new MinecraftBukkitInteractionMenuHolder(target, menu, feedback);
        Inventory inventory = Bukkit.createInventory(holder, menu.size(), ChatColor.GOLD + menu.title());
        holder.inventory(inventory);
        fill(inventory);
        inventory.setItem(4, header(menu, feedback));
        for (InteractionMenuElement element : menu.elements()) {
            inventory.setItem(element.slot(), button(element));
        }
        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(MinecraftBukkitServerPlugin.class), () -> player.openInventory(inventory));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MinecraftBukkitInteractionMenuHolder holder)) {
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
        if (action == null || action.isBlank()) {
            return;
        }
        String elementId = meta.getPersistentDataContainer().get(ELEMENT_KEY, PersistentDataType.STRING);
        String payload = meta.getPersistentDataContainer().get(PAYLOAD_KEY, PersistentDataType.STRING);
        handleAction(player, holder, action, elementId == null ? "" : elementId, payload == null ? "" : payload);
    }

    private void handleAction(Player player, MinecraftBukkitInteractionMenuHolder holder, String action, String elementId, String payload) {
        if (org.tavall.minecraft.framework.game.ui.UiActions.CLOSE.equals(action)) {
            player.closeInventory();
            return;
        }
        Map<String, String> context = new LinkedHashMap<String, String>(holder.menu().metadata());
        context.putAll(holder.target().metadata());
        context.put("menuId", holder.menu().menuId());
        context.put("elementId", elementId);
        context.put("actionId", action);
        if (payload != null && !payload.isBlank()) {
            context.put("payload", payload);
        }
        if (holder.target().targetType() != null) {
            context.put("targetType", holder.target().targetType().name());
        }
        context.put("targetId", holder.target().targetId());

        InteractionRequest request = new InteractionRequest(
                "minecraft-interaction-" + UUID.randomUUID(),
                player.getUniqueId().toString(),
                holder.menu().targetType(),
                holder.menu().targetId(),
                "execute_action",
                getMinecraftBukkitServerConfig().serverId(),
                player.getWorld().getName(),
                context,
                Instant.now().toEpochMilli()
        );

        try {
            InteractionResult result = getMinecraftBukkitCommandClientHandler().submitInteraction(request);
            if (result.menu() != null) {
                open(player, result.menu(), result.message());
                return;
            }
            if (result.success()) {
                player.sendMessage(ChatColor.GREEN + result.message());
                if (result.resultType() == InteractionResultType.EXECUTE_ACTION && !result.message().isBlank()) {
                    player.closeInventory();
                }
            } else {
                String message = result.message();
                if (result.disabledReason() != null && !result.disabledReason().isBlank()) {
                    message = message + " " + result.disabledReason();
                }
                player.sendMessage(ChatColor.RED + message);
            }
        } catch (Exception exception) {
            player.sendMessage(ChatColor.RED + "Interaction failed: " + exception.getMessage());
        }
    }

    private ItemStack button(InteractionMenuElement element) {
        Material material = Material.matchMaterial(element.material());
        if (material == null) {
            material = Material.PAPER;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName((element.enabled() ? ChatColor.GOLD : ChatColor.DARK_RED) + element.displayName());
            ArrayList<String> lore = new ArrayList<String>();
            if (element.lore() != null) {
                for (String line : element.lore()) {
                    if (line != null && !line.isBlank()) {
                        lore.add(ChatColor.GRAY + line);
                    }
                }
            }
            if (!element.enabled() && element.disabledReason() != null && !element.disabledReason().isBlank()) {
                lore.add(ChatColor.RED + element.disabledReason());
            }
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, element.actionId() == null ? "" : element.actionId());
            meta.getPersistentDataContainer().set(ELEMENT_KEY, PersistentDataType.STRING, element.elementId());
            if (element.metadata() != null && element.metadata().containsKey("payload")) {
                meta.getPersistentDataContainer().set(PAYLOAD_KEY, PersistentDataType.STRING, element.metadata().get("payload"));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack header(InteractionMenuModel menu, String feedback) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + menu.title());
            ArrayList<String> lore = new ArrayList<String>();
            if (feedback != null && !feedback.isBlank()) {
                lore.add(ChatColor.GRAY + feedback);
            }
            lore.add(ChatColor.DARK_GRAY + String.valueOf(menu.targetType()) + " " + menu.targetId());
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
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
}
