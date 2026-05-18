package org.tavall.control.dependency.interfaces;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.control.domain.CastleLocationData;
import org.tavall.control.domain.PlayerGameState;

import java.time.Instant;
import java.util.UUID;

/**
 * Owns persisted castle relocation for a player.
 */
public interface ICastlePlacementService extends IDependencyInjectableInterface {
    PlayerGameState placeCastle(UUID playerId, CastleLocationData castleLocation, Instant now);
}
