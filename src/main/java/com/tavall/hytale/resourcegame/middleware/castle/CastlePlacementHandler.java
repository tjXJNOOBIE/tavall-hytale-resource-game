package com.tavall.hytale.resourcegame.middleware.castle;

import com.tavall.hytale.resourcegame.middleware.common.CanonicalLocation;

public final class CastlePlacementHandler {
    private final CastleRepository castleRepository;

    public CastlePlacementHandler(CastleRepository castleRepository) {
        this.castleRepository = castleRepository;
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
        return castleRepository.saveCastle(placed);
    }
}
