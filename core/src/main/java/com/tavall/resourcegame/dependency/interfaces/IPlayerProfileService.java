package com.tavall.resourcegame.dependency.interfaces;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.domain.PlayerProfile;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface IPlayerProfileService extends IDependencyInjectableInterface {
    Optional<PlayerProfile> readCached(UUID playerId);

    PlayerProfile loadOrCreate(UUID playerId, String name, String timezone, String ipHash, Instant now);

    void persist(PlayerProfile profile, Instant now);
}