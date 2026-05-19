package org.tavall.minecraft.server.ui.inventory;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class CrownboundGuiItemFactory {
    public static final String FAMILY_PRIMARY = "primary";
    public static final String FAMILY_SECONDARY = "secondary";
    public static final String FAMILY_DANGER = "danger";
    public static final String FAMILY_SUCCESS = "success";
    public static final String FAMILY_TAB = "tab";
    public static final String FAMILY_ICON = "icon";

    public Component commandCenterTitle() {
        return Component.text()
                .append(Component.text("\uE001").font(Key.key("crownbound", "gui")))
                .append(Component.text(" Kingdom Command Center", NamedTextColor.GOLD))
                .build();
    }

    public ItemStack header(String title, String feedback) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.displayName(Component.text(title, NamedTextColor.GOLD));
        ArrayList<Component> lore = new ArrayList<Component>();
        if (feedback != null && !feedback.isBlank()) {
            lore.add(Component.text(feedback, NamedTextColor.GRAY));
        }
        lore.add(Component.text("Crownbound command center", NamedTextColor.DARK_AQUA));
        lore.add(Component.text("Use the tabs to move through kingdom systems.", NamedTextColor.DARK_GRAY));
        meta.lore(lore);
        meta.setItemModel(new NamespacedKey("crownbound", "ui/button_icon"));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack button(Material material, String title, List<String> lore, String buttonFamily) {
        ItemStack item = new ItemStack(material == null ? Material.PAPER : material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.displayName(Component.text(title, colorForFamily(buttonFamily)));
        ArrayList<Component> loreLines = new ArrayList<Component>();
        for (String line : lore == null ? List.<String>of() : lore) {
            if (line != null && !line.isBlank()) {
                loreLines.add(Component.text(line, NamedTextColor.GRAY));
            }
        }
        if (buttonFamily != null && !buttonFamily.isBlank()) {
            loreLines.add(Component.text("Crownbound button: " + familyLabel(buttonFamily), NamedTextColor.DARK_AQUA));
        }
        meta.lore(loreLines);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        NamespacedKey itemModelKey = itemModelForFamily(buttonFamily);
        if (itemModelKey != null) {
            meta.setItemModel(itemModelKey);
        }
        item.setItemMeta(meta);
        return item;
    }

    private NamedTextColor colorForFamily(String buttonFamily) {
        if (buttonFamily == null || buttonFamily.isBlank()) {
            return NamedTextColor.GOLD;
        }
        return switch (buttonFamily) {
            case FAMILY_PRIMARY -> NamedTextColor.GOLD;
            case FAMILY_SECONDARY -> NamedTextColor.AQUA;
            case FAMILY_DANGER -> NamedTextColor.RED;
            case FAMILY_SUCCESS -> NamedTextColor.GREEN;
            case FAMILY_TAB -> NamedTextColor.YELLOW;
            case FAMILY_ICON -> NamedTextColor.LIGHT_PURPLE;
            default -> NamedTextColor.GOLD;
        };
    }

    private String familyLabel(String buttonFamily) {
        if (buttonFamily == null || buttonFamily.isBlank()) {
            return "Default";
        }
        return switch (buttonFamily) {
            case FAMILY_PRIMARY -> "Primary";
            case FAMILY_SECONDARY -> "Secondary";
            case FAMILY_DANGER -> "Danger";
            case FAMILY_SUCCESS -> "Success";
            case FAMILY_TAB -> "Tab";
            case FAMILY_ICON -> "Icon";
            default -> buttonFamily;
        };
    }

    private NamespacedKey itemModelForFamily(String buttonFamily) {
        if (buttonFamily == null || buttonFamily.isBlank()) {
            return null;
        }
        return switch (buttonFamily) {
            case FAMILY_PRIMARY -> new NamespacedKey("crownbound", "ui/button_primary");
            case FAMILY_SECONDARY -> new NamespacedKey("crownbound", "ui/button_secondary");
            case FAMILY_DANGER -> new NamespacedKey("crownbound", "ui/button_danger");
            case FAMILY_SUCCESS -> new NamespacedKey("crownbound", "ui/button_success");
            case FAMILY_TAB -> new NamespacedKey("crownbound", "ui/button_tab");
            case FAMILY_ICON -> new NamespacedKey("crownbound", "ui/button_icon");
            default -> null;
        };
    }
}
