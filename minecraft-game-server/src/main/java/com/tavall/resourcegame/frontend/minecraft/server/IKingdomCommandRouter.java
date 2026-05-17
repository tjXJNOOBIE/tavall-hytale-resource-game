package com.tavall.resourcegame.frontend.minecraft.server;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

public interface IKingdomCommandRouter extends CommandExecutor, TabCompleter, IDependencyInjectableInterface {
}
