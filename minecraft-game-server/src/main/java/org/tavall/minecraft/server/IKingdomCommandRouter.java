package org.tavall.minecraft.server;

import org.tavall.dependency.IDependencyInjectableInterface;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

public interface IKingdomCommandRouter extends CommandExecutor, TabCompleter, IDependencyInjectableInterface {
}
