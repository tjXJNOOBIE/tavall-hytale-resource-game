package com.tavall.resourcegame.middleware.healing;

public enum HealingFacilityType {
    FIELD_TENT("Field Tent", "field_tent"),
    INFIRMARY("Infirmary", "infirmary"),
    HERBALIST_HUT("Herbalist Hut", "herbalist_hut"),
    APOTHECARY("Apothecary", "apothecary"),
    FIELD_HOSPITAL("Field Hospital", "field_hospital"),
    SURGICAL_HALL("Surgical Hall", "surgical_hall"),
    SHRINE("Shrine", "shrine"),
    GUILD_HOSPITAL("Guild Hospital", "guild_hospital");

    private final String displayName;
    private final String assetKey;

    HealingFacilityType(String displayName, String assetKey) {
        this.displayName = displayName;
        this.assetKey = assetKey;
    }

    public String displayName() {
        return displayName;
    }

    public String assetKey() {
        return assetKey;
    }
}
