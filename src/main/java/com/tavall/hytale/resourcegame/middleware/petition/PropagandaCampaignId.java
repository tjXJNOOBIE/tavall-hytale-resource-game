package com.tavall.hytale.resourcegame.middleware.petition;

import java.util.Objects;
import java.util.UUID;

public record PropagandaCampaignId(UUID value) {
    public PropagandaCampaignId {
        Objects.requireNonNull(value, "value");
    }

    public static PropagandaCampaignId random() {
        return new PropagandaCampaignId(UUID.randomUUID());
    }
}
