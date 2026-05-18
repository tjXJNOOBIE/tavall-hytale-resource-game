package org.tavall.minecraft.server;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

public interface IMinecraftBukkitCommandHandler extends CommandExecutor, TabCompleter, IDependencyInjectableInterface {
}
