package org.tavall.minecraft.server;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class KingdomNpcCommand extends KingdomForwardingCommand {
    private final KingdomNpcGui npcGui = new KingdomNpcGui();

    @Override
    protected String rootToken() {
        return "npc";
    }

    @Override
    protected String usage() {
        return "Usage: /kd npc [open|debug|building|close]";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("open", "debug", "building", "close");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                return npcGui.open(player);
            }
            sender.sendMessage(usage());
            return true;
        }
        return super.onCommand(sender, command, label, args);
    }
}
