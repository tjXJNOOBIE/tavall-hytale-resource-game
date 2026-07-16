package org.tavall.minecraft.server;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class KingdomAccountCommand extends KingdomForwardingCommand {
    private final KingdomAccountGui accountGui = new KingdomAccountGui();

    @Override
    protected String rootToken() {
        return "account";
    }

    @Override
    protected String usage() {
        return "Usage: /kd account [status|addxp|setlevel|debug]";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("status", "addxp", "setlevel", "debug");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || "status".equalsIgnoreCase(args[0])) {
            if (sender instanceof Player player) {
                return accountGui.open(player, "kd account");
            }
            sender.sendMessage(usage());
            return true;
        }
        return super.onCommand(sender, command, label, args);
    }
}
