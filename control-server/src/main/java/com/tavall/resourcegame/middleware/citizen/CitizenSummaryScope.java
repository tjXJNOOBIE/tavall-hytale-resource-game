package com.tavall.resourcegame.middleware.citizen;

import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

import java.util.Objects;

public record CitizenSummaryScope(CitizenSummaryScopeType scopeType, String scopeId) {
    public CitizenSummaryScope {
        Objects.requireNonNull(scopeType, "scopeType");
        if (scopeId == null || scopeId.isBlank()) {
            throw new IllegalArgumentException("scopeId is required.");
        }
    }

    public static CitizenSummaryScope player(UniversalPlayerId playerId) {
        return new CitizenSummaryScope(CitizenSummaryScopeType.PLAYER, playerId.toString());
    }

    public static CitizenSummaryScope kingdom(String kingdomId) {
        return new CitizenSummaryScope(CitizenSummaryScopeType.KINGDOM, kingdomId);
    }

    public String cacheKey() {
        return scopeType.name().toLowerCase() + ":" + scopeId;
    }
}
