package com.tavall.resourcegame.middleware.healing;

import com.tavall.resourcegame.middleware.asset.GlobalAssetId;

public enum GemType {
    PEARL(GemDomain.PROTECTION, "resource.gem.pearl"),
    AMETHYST(GemDomain.MAGIC, "resource.gem.amethyst"),
    PERIDOT(GemDomain.ECONOMY, "resource.gem.peridot"),
    RUBY(GemDomain.COMBAT, "resource.gem.ruby"),
    SAPPHIRE(GemDomain.INTELLIGENCE, "resource.gem.sapphire");

    private final GemDomain domain;
    private final GlobalAssetId globalAssetId;

    GemType(GemDomain domain, String globalAssetId) {
        this.domain = domain;
        this.globalAssetId = new GlobalAssetId(globalAssetId);
    }

    public GemDomain domain() {
        return domain;
    }

    public GlobalAssetId globalAssetId() {
        return globalAssetId;
    }
}
