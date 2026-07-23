package org.tavall.minecraft.server;

import org.tavall.minecraft.framework.game.ui.UiScreenKey;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public final class KingdomHelpCommand implements CommandExecutor, TabCompleter, MinecraftBukkitServerDomain, org.tavall.dependency.IDependencyInjectableConcrete {
    private static final String ADMIN_PERMISSION = "tavall.resourcegame.admin";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && !"list".equalsIgnoreCase(args[0])) {
            sender.sendMessage(ChatColor.YELLOW + "Use /kd help list to show the command list.");
        }
        helpLines(sender.hasPermission(ADMIN_PERMISSION)).forEach(sender::sendMessage);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length <= 1) {
            return List.of("list");
        }
        return List.of();
    }

    static List<String> helpLines(boolean admin) {
        ArrayList<String> lines = new ArrayList<String>();
        lines.add(ChatColor.GOLD + "Kingdom Commands");
        lines.add(ChatColor.GRAY + "/kd help list" + ChatColor.DARK_GRAY + " - show this menu");
        lines.add(ChatColor.GRAY + "/kd ui" + ChatColor.DARK_GRAY + " - open the kingdom command center");
        lines.add(ChatColor.GRAY + "/kd ui account" + ChatColor.DARK_GRAY + " - open the player data profile");
        lines.add(ChatColor.GRAY + "/kd castle" + ChatColor.DARK_GRAY + " - castle routing and placement");
        lines.add(ChatColor.GRAY + "/kd citizens" + ChatColor.DARK_GRAY + " - citizen progression and staffing");
        lines.add(ChatColor.GRAY + "/kd troops" + ChatColor.DARK_GRAY + " - troop health and promotion controls");
        lines.add(ChatColor.GRAY + "/kd resources" + ChatColor.DARK_GRAY + " - resource grants and balance control");
        lines.add(ChatColor.GRAY + "/kd companion" + ChatColor.DARK_GRAY + " - companion management and training");
        lines.add(ChatColor.GRAY + "/kd npc" + ChatColor.DARK_GRAY + " - npc interaction and building entry");
        lines.add(ChatColor.GRAY + "/kd building" + ChatColor.DARK_GRAY + " - focused building controls");
        lines.add(ChatColor.GRAY + "/kd account" + ChatColor.DARK_GRAY + " - player progression and debug state");
        lines.add(ChatColor.GRAY + "/kd data" + ChatColor.DARK_GRAY + " - player data profile alias");
        lines.add(ChatColor.GRAY + "/kd buildings" + ChatColor.DARK_GRAY + " - building staging and lifecycle control");
        lines.add(ChatColor.GRAY + "/kd nodes" + ChatColor.DARK_GRAY + " - resource node placement and allocation");
        lines.add(ChatColor.GRAY + "/kd place" + ChatColor.DARK_GRAY + " - placement workflow and confirmations");
        lines.add(ChatColor.GRAY + "/kd interior" + ChatColor.DARK_GRAY + " - interior entry and exit flow");
        lines.add(ChatColor.GRAY + "/kd scene" + ChatColor.DARK_GRAY + " - scene refresh and presentation hooks");
        lines.add(ChatColor.GRAY + "/kd bootstrap" + ChatColor.DARK_GRAY + " - world bootstrap and setup actions");
        lines.add(ChatColor.GRAY + "/kd tick" + ChatColor.DARK_GRAY + " - simulation and healing ticks");
        if (admin) {
            lines.add(ChatColor.DARK_PURPLE + "Admin surfaces");
            lines.add(ChatColor.GRAY + "/kd debug" + ChatColor.DARK_GRAY + " - debug command center and live tools");
            lines.add(ChatColor.GRAY + "/kd hologram" + ChatColor.DARK_GRAY + " - hologram diagnostics");
            lines.add(ChatColor.GRAY + "/kd entity" + ChatColor.DARK_GRAY + " - entity spawning and clearing");
        }
        return List.copyOf(lines);
    }
}
