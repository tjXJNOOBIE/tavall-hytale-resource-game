package com.tavall.hytale.resourcegame.middleware.troop;

import com.tavall.hytale.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.hytale.resourcegame.middleware.common.CanonicalLocation;
import com.tavall.hytale.resourcegame.middleware.guild.GuildId;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.util.Map;
import java.util.Optional;

public final class TroopRegistrationHandler {
    private final TroopRepository troopRepository;

    public TroopRegistrationHandler(TroopRepository troopRepository) {
        this.troopRepository = troopRepository;
    }

    public Troop registerTroop(Optional<UniversalPlayerId> ownerPlayerId, Optional<GuildId> ownerGuildId, String troopType, int tier, CanonicalLocation location) {
        Troop troop = new Troop(
                TroopId.random(),
                ownerPlayerId,
                ownerGuildId,
                troopType,
                tier,
                100,
                TroopStatus.IDLE,
                location,
                Optional.empty(),
                new GlobalAssetId("troop." + (troopType == null ? "infantry" : troopType.toLowerCase()) + ".tier_" + tier),
                Map.of()
        );
        return troopRepository.saveTroop(troop);
    }
}
