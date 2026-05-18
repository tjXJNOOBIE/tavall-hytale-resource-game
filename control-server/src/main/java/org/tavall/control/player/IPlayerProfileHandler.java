package org.tavall.control.player;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.control.domain.PlayerProfile;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface IPlayerProfileHandler extends IDependencyInjectableInterface {
    Optional<PlayerProfile> readCached(UUID playerId);

    PlayerProfile loadOrCreate(UUID playerId, String name, String timezone, String ipHash, Instant now);

    void persist(PlayerProfile profile, Instant now);
}
