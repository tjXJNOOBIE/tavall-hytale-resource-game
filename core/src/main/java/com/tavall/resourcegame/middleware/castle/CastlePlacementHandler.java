package com.tavall.resourcegame.middleware.castle;

import com.tavall.resourcegame.middleware.common.CanonicalLocation;

public final class CastlePlacementHandler implements ICastleDomain {
    public CastlePlacementHandler() {
    }

    public CastlePlacementHandler(CastleRepository castleRepository) {
        registerCastleRepository(castleRepository);
    }

    public Castle placeCastle(Castle castle, CanonicalLocation location) {
        Castle placed = new Castle(
                castle.castleId(),
                castle.ownerPlayerId(),
                castle.guildId(),
                location,
                castle.castleType(),
                castle.level(),
                CastleState.ACTIVE,
                castle.resourceGenerators(),
                castle.defensiveStats(),
                castle.globalAssetId(),
                castle.metadata()
        );
        return getCastleRepository().saveCastle(placed);
    }
}
