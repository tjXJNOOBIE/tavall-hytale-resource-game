package com.tavall.hytale.resourcegame.middleware.castle;

import com.tavall.hytale.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.hytale.resourcegame.middleware.common.CanonicalLocation;
import com.tavall.hytale.resourcegame.middleware.common.MetadataMaps;
import com.tavall.hytale.resourcegame.middleware.guild.GuildId;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record Castle(
        CastleId castleId,
        UniversalPlayerId ownerPlayerId,
        Optional<GuildId> guildId,
        CanonicalLocation location,
        CastleType castleType,
        int level,
        CastleState state,
        List<String> resourceGenerators,
        DefensiveStats defensiveStats,
        GlobalAssetId globalAssetId,
        Map<String, String> metadata
) {
    public Castle {
        Objects.requireNonNull(castleId, "castleId");
        Objects.requireNonNull(ownerPlayerId, "ownerPlayerId");
        guildId = guildId == null ? Optional.empty() : guildId;
        Objects.requireNonNull(location, "location");
        castleType = castleType == null ? CastleType.BASIC : castleType;
        level = Math.max(1, level);
        state = state == null ? CastleState.PLANNED : state;
        resourceGenerators = resourceGenerators == null ? List.of() : List.copyOf(resourceGenerators);
        defensiveStats = defensiveStats == null ? new DefensiveStats(0, 0, 0) : defensiveStats;
        Objects.requireNonNull(globalAssetId, "globalAssetId");
        metadata = MetadataMaps.immutable(metadata);
    }
}
