package com.tavall.resourcegame.middleware.troop;

import com.tavall.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.resourcegame.middleware.common.CanonicalLocation;
import com.tavall.resourcegame.middleware.guild.GuildId;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

import java.util.Map;
import java.util.Optional;

public final class TroopRegistrationHandler implements ITroopDomain {
    public TroopRegistrationHandler() {
    }

    public TroopRegistrationHandler(TroopRepository troopRepository) {
        registerTroopRepository(troopRepository);
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
        return getTroopRepository().saveTroop(troop);
    }
}
