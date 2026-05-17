package com.tavall.resourcegame.dependency.interfaces;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.domain.CastleLocationData;

public interface ICastlePromptLaneService extends IDependencyInjectableInterface {
    void alignPlayer(Player player, CastleLocationData castleLocation);
}