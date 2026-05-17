package com.tavall.resourcegame.dependency.interfaces;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.domain.CastleLocationData;
import com.tavall.resourcegame.domain.PlayerGameState;

import java.time.Instant;
import java.util.UUID;

/**
 * Owns persisted castle relocation for a player.
 */
public interface ICastlePlacementService extends IDependencyInjectableInterface {
    PlayerGameState placeCastle(UUID playerId, CastleLocationData castleLocation, Instant now);
}
