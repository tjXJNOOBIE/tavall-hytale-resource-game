package org.tavall.control.farmstead.ui;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.dependency.IDependencyInjectableInterface;

public interface IFarmsteadMenuHandler extends IDependencyInjectableInterface {
    boolean openFarmsteadMenu(Player player);
}

