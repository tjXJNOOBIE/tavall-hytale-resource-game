package com.tavall.resourcegame.dependency.interfaces;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IFarmsteadMenuService extends IDependencyInjectableInterface {
    boolean openFarmsteadMenu(Player player);
}
