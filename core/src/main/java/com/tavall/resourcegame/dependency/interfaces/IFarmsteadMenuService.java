package com.tavall.resourcegame.dependency.interfaces;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

public interface IFarmsteadMenuService extends IDependencyInjectableInterface {
    boolean openFarmsteadMenu(Player player);
}
