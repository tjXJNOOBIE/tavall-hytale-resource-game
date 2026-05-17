package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.bukkit.entity.Player;

import java.util.Optional;

public interface IMinecraftBukkitStructureWorldActionHandler extends IDependencyInjectableInterface {
    Optional<String> apply(Player player, String[] tokens, FrontendCommandVerificationResult result);
}
