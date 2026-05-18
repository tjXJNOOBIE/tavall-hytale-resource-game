package org.tavall.control.castle;

import org.tavall.control.asset.GlobalAssetId;
import org.tavall.control.common.CanonicalLocation;
import org.tavall.control.guild.GuildId;
import org.tavall.control.identity.UniversalPlayerId;

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
