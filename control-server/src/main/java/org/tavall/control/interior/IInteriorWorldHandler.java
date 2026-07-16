package org.tavall.control.interior;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IInteriorWorldHandler extends IDependencyInjectableInterface {
    void enterInterior(Player player);

    void exitInterior(Player player);

    void generateInterior(Player player);

    void rebuildInterior(Player player);

    void deleteInterior(Player player);

    void moveInterior(Player player);
}

