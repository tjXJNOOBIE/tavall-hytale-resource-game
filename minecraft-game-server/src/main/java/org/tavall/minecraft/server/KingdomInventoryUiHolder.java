package org.tavall.minecraft.server;

import org.tavall.minecraft.framework.game.ui.UiScreenKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Objects;

final class KingdomInventoryUiHolder implements InventoryHolder {
    private final UiScreenKey pageType;
    private final String feedback;

    KingdomInventoryUiHolder(UiScreenKey pageType, String feedback) {
        this.pageType = Objects.requireNonNull(pageType, "pageType");
        this.feedback = feedback == null ? "" : feedback;
    }

    UiScreenKey pageType() {
        return pageType;
    }

    String feedback() {
        return feedback;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
