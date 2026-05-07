package com.tavall.hytale.resourcegame.middleware.castle;

import com.tavall.hytale.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.hytale.resourcegame.middleware.common.CanonicalLocation;
import com.tavall.hytale.resourcegame.middleware.guild.GuildId;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CastleCreationHandler {
    private final CastleRepository castleRepository;

    public CastleCreationHandler(CastleRepository castleRepository) {
        this.castleRepository = castleRepository;
    }

    public Castle createCastleForUniversalPlayer(
            UniversalPlayerId ownerPlayerId,
            Optional<GuildId> guildId,
            CanonicalLocation location
    ) {
        Castle castle = new Castle(
                CastleId.random(),
                ownerPlayerId,
                guildId,
                location,
                CastleType.BASIC,
                1,
                CastleState.ACTIVE,
                List.of("wood", "food"),
                new DefensiveStats(10, 5, 2),
                new GlobalAssetId("castle.basic.level_1"),
                Map.of()
        );
        return castleRepository.saveCastle(castle);
    }
}
