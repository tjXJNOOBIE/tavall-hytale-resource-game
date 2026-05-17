package com.tavall.resourcegame.frontend.minecraft.server;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class KingdomDataCommand extends KingdomForwardingCommand {
    private final KingdomAccountGui accountGui = new KingdomAccountGui();

    @Override
    protected String rootToken() {
        return "data";
    }

    @Override
    protected String usage() {
        return "Usage: /kd data [status]";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("status");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || "status".equalsIgnoreCase(args[0])) {
            if (sender instanceof Player player) {
                return accountGui.open(player, "kd data status");
            }
            sender.sendMessage(usage());
            return true;
        }
        sender.sendMessage(usage());
        return true;
    }
}
