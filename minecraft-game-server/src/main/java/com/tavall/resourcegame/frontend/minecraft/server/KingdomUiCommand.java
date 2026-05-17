package com.tavall.resourcegame.frontend.minecraft.server;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class KingdomUiCommand extends KingdomForwardingCommand {
    private final KingdomAdminGui adminGui = new KingdomAdminGui();
    private final KingdomCastleGui castleGui = new KingdomCastleGui();
    private final KingdomCitizensGui citizensGui = new KingdomCitizensGui();
    private final KingdomTroopsGui troopsGui = new KingdomTroopsGui();
    private final KingdomResourcesGui resourcesGui = new KingdomResourcesGui();
    private final KingdomBuildingsGui buildingsGui = new KingdomBuildingsGui();
    private final KingdomBuildingGui buildingGui = new KingdomBuildingGui();
    private final KingdomAccountGui accountGui = new KingdomAccountGui();
    private final KingdomNpcGui npcGui = new KingdomNpcGui();
    private final KingdomInteriorGui interiorGui = new KingdomInteriorGui();
    private final KingdomPlacementGui placementGui = new KingdomPlacementGui();
    private final KingdomDebugGui debugGui = new KingdomDebugGui();

    @Override
    protected String rootToken() {
        return "ui";
    }

    @Override
    protected String usage() {
        return "Usage: /kd ui [help|admin|castle|citizens|troops|resources|account|buildings|building|npc|interior|placement|debug]";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("help", "admin", "castle", "citizens", "troops", "resources", "account", "buildings", "building", "npc", "interior", "placement", "debug");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command requires a player sender.");
            return true;
        }
        String token = args.length == 0 ? "admin" : args[0];
        if (!open(player, token)) {
            sender.sendMessage(usage());
            return true;
        }
        return true;
    }

    private boolean open(Player player, String token) {
        return switch (token.toLowerCase()) {
            case "admin", "help", "main" -> adminGui.open(player);
            case "castle" -> castleGui.open(player);
            case "citizens" -> citizensGui.open(player);
            case "troops" -> troopsGui.open(player);
            case "resources" -> resourcesGui.open(player);
            case "account" -> accountGui.open(player, "kd ui account");
            case "buildings" -> buildingsGui.open(player);
            case "building" -> buildingGui.open(player);
            case "npc" -> npcGui.open(player);
            case "interior" -> interiorGui.open(player);
            case "placement" -> placementGui.open(player);
            case "debug" -> debugGui.open(player);
            default -> false;
        };
    }
}
