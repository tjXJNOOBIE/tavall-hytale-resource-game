package org.tavall.minecraft.server;

import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.bukkit.entity.Player;

import java.util.Optional;

public interface IMinecraftBukkitWorldActionHandler extends IDependencyInjectableInterface {
    Optional<String> apply(Player player, String rawInput, FrontendCommandVerificationResult result);
}
