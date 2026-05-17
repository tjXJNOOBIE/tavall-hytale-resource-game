package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.api.internal.interaction.InteractionMenuModel;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;

public interface IMinecraftBukkitInteractionMenuHandler extends Listener, IDependencyInjectableInterface {
    void open(Player player, InteractionMenuModel menu, String feedback);
}
