package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.ui.UiPageType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Objects;

final class KingdomInventoryUiHolder implements InventoryHolder {
    private final UiPageType pageType;
    private final String feedback;

    KingdomInventoryUiHolder(UiPageType pageType, String feedback) {
        this.pageType = Objects.requireNonNull(pageType, "pageType");
        this.feedback = feedback == null ? "" : feedback;
    }

    UiPageType pageType() {
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
