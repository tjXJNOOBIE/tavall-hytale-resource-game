package org.tavall.minecraft.server.ui.inventory;

import org.bukkit.inventory.ItemStack;

public final class GuiButton {
    private final int slot;
    private final ItemStack displayItem;
    private final GuiClickAction clickAction;

    public GuiButton(int slot, ItemStack displayItem, GuiClickAction clickAction) {
        this.slot = slot;
        this.displayItem = displayItem;
        this.clickAction = clickAction;
    }

    public int slot() {
        return slot;
    }

    public ItemStack displayItem() {
        return displayItem;
    }

    public void click(GuiClickContext context) {
        clickAction.onClick(context);
    }
}
