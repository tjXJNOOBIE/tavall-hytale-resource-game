package org.tavall.control.castle;

import org.tavall.control.identity.UniversalPlayerId;

import java.util.List;
import java.util.Optional;

public interface CastleRepository {
    Castle saveCastle(Castle castle);

    Optional<Castle> findCastle(CastleId castleId);

    List<Castle> findCastlesForPlayer(UniversalPlayerId universalPlayerId);
}
