package org.tavall.control.castle;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface ICastleInteractionHandler extends IDependencyInjectableInterface {
    void handleInteract(PlayerInteractEvent event);

    boolean isPlayerFocusingOwnedCastle(Player player);

    void openCastleUi(Player player);
}
