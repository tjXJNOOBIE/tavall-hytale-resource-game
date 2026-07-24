package org.tavall.minecraft.server;

import org.tavall.minecraft.framework.game.ui.UiScreenKey;
import org.tavall.minecraft.server.commands.support.KingdomCommandSupport;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Optional;
import java.util.List;

abstract class KingdomForwardingCommand implements CommandExecutor, TabCompleter, MinecraftBukkitServerDomain, org.tavall.dependency.IDependencyInjectableConcrete {
    protected abstract String rootToken();

    protected abstract String usage();

    protected Optional<UiScreenKey> defaultPage() {
        return Optional.empty();
    }

    protected List<String> subcommands() {
        return List.of();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            Optional<UiScreenKey> defaultPage = defaultPage();
            if (defaultPage.isPresent()) {
                return KingdomCommandSupport.openPageIfPlayer(this, sender, defaultPage.get(), "");
            }
            sender.sendMessage(usage());
            return true;
        }
        return KingdomCommandSupport.forward(this, sender, label, rootToken(), args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length <= 1) {
            return KingdomCommandSupport.matching(subcommands(), args.length == 0 ? "" : args[0]);
        }
        return List.of();
    }
}
