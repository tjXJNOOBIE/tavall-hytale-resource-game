package org.tavall.control.castle;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.dependency.IDependencyInjectableInterface;
import org.tavall.control.domain.CastleLocationData;

public interface ICastlePromptLaneHandler extends IDependencyInjectableInterface {
    void alignPlayer(Player player, CastleLocationData castleLocation);
}
