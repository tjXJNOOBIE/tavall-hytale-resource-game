package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.api.internal.frontend.FrontendCommandVerificationResult;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.bukkit.entity.Player;

import java.util.Optional;

public interface IMinecraftBukkitWorldActionHandler extends IDependencyInjectableInterface {
    Optional<String> apply(Player player, String rawInput, FrontendCommandVerificationResult result);
}
