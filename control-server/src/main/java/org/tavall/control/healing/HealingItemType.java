package org.tavall.control.healing;

import org.tavall.control.asset.GlobalAssetId;

public enum HealingItemType {
    FIELD_RATIONS("item.healing.field_rations"),
    BANDAGE_KIT("item.healing.bandage_kit"),
    ANTIDOTE_KIT("item.healing.antidote_kit"),
    ARCANE_SALVE("item.healing.arcane_salve");

    private final GlobalAssetId globalAssetId;

    HealingItemType(String globalAssetId) {
        this.globalAssetId = new GlobalAssetId(globalAssetId);
    }

    public GlobalAssetId globalAssetId() {
        return globalAssetId;
    }
}
