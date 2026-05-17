package com.tavall.resourcegame.middleware.castle;

import com.tavall.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.resourcegame.middleware.common.CanonicalLocation;
import com.tavall.resourcegame.middleware.guild.GuildId;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CastleCreationHandler implements ICastleDomain {
    public CastleCreationHandler() {
    }

    public CastleCreationHandler(CastleRepository castleRepository) {
        registerCastleRepository(castleRepository);
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
        return getCastleRepository().saveCastle(castle);
    }
}
