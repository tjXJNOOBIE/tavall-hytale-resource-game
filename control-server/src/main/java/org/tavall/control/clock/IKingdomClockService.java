package org.tavall.control.clock;

import com.hypixel.hytale.server.core.universe.world.World;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.control.domain.KingdomClockState;

public interface IKingdomClockService extends IDependencyInjectableInterface {
    KingdomClockState snapshot();

    void applyToWorld(World world);

    void applyToAllWorlds();

    void start();

    void shutdown();
}

