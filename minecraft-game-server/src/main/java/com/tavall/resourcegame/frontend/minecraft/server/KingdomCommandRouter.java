package org.tavall.minecraft.server;

import org.tavall.minecraft.server.commands.util.KingdomCommandSupport;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.Locale;

public final class KingdomCommandRouter implements IKingdomCommandRouter, IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    private final KingdomHelpCommand helpCommand = new KingdomHelpCommand();
    private final KingdomUiCommand uiCommand = new KingdomUiCommand();
    private final KingdomDataCommand dataCommand = new KingdomDataCommand();
    private final KingdomCastleCommand castleCommand = new KingdomCastleCommand();
    private final KingdomCitizensCommand citizensCommand = new KingdomCitizensCommand();
    private final KingdomTroopsCommand troopsCommand = new KingdomTroopsCommand();
    private final KingdomResourcesCommand resourcesCommand = new KingdomResourcesCommand();
    private final KingdomAccountCommand accountCommand = new KingdomAccountCommand();
    private final KingdomBuildingsCommand buildingsCommand = new KingdomBuildingsCommand();
    private final KingdomNodesCommand nodesCommand = new KingdomNodesCommand();
    private final KingdomPlaceCommand placeCommand = new KingdomPlaceCommand();
    private final KingdomInteriorCommand interiorCommand = new KingdomInteriorCommand();
    private final KingdomHologramCommand hologramCommand = new KingdomHologramCommand();
    private final KingdomEntityCommand entityCommand = new KingdomEntityCommand();
    private final KingdomSceneCommand sceneCommand = new KingdomSceneCommand();
    private final KingdomBootstrapCommand bootstrapCommand = new KingdomBootstrapCommand();
    private final KingdomTickCommand tickCommand = new KingdomTickCommand();
    private final KingdomDebugCommand debugCommand = new KingdomDebugCommand();
    private final KingdomCompanionCommand companionCommand = new KingdomCompanionCommand();
    private final KingdomNpcCommand npcCommand = new KingdomNpcCommand();
    private final KingdomBuildingCommand buildingCommand = new KingdomBuildingCommand();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return helpCommand.onCommand(sender, command, label, args);
        }
        String root = args[0].toLowerCase(Locale.ROOT);
        String[] subArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
        return switch (root) {
            case "help" -> helpCommand.onCommand(sender, command, label, subArgs);
            case "ui" -> uiCommand.onCommand(sender, command, label, subArgs);
            case "data" -> dataCommand.onCommand(sender, command, label, subArgs);
            case "castle" -> castleCommand.onCommand(sender, command, label, subArgs);
            case "citizens" -> citizensCommand.onCommand(sender, command, label, subArgs);
            case "troops" -> troopsCommand.onCommand(sender, command, label, subArgs);
            case "resources" -> resourcesCommand.onCommand(sender, command, label, subArgs);
            case "account" -> accountCommand.onCommand(sender, command, label, subArgs);
            case "buildings" -> buildingsCommand.onCommand(sender, command, label, subArgs);
            case "nodes" -> nodesCommand.onCommand(sender, command, label, subArgs);
            case "place" -> placeCommand.onCommand(sender, command, label, subArgs);
            case "interior" -> interiorCommand.onCommand(sender, command, label, subArgs);
            case "hologram" -> hologramCommand.onCommand(sender, command, label, subArgs);
            case "entity", "entities" -> entityCommand.onCommand(sender, command, label, subArgs);
            case "scene" -> sceneCommand.onCommand(sender, command, label, subArgs);
            case "bootstrap" -> bootstrapCommand.onCommand(sender, command, label, subArgs);
            case "tick" -> tickCommand.onCommand(sender, command, label, subArgs);
            case "debug" -> debugCommand.onCommand(sender, command, label, subArgs);
            case "companion", "companions" -> companionCommand.onCommand(sender, command, label, subArgs);
            case "npc" -> npcCommand.onCommand(sender, command, label, subArgs);
            case "building" -> buildingCommand.onCommand(sender, command, label, subArgs);
            default -> {
                yield helpCommand.onCommand(sender, command, label, args);
            }
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            return List.of("help", "ui", "data", "castle", "citizens", "troops", "resources", "account", "buildings", "building", "nodes", "place", "interior", "hologram", "entity", "scene", "bootstrap", "tick", "debug", "companion", "npc");
        }
        if (args.length == 1) {
            return KingdomCommandSupport.matching(List.of("help", "ui", "data", "castle", "citizens", "troops", "resources", "account", "buildings", "building", "nodes", "place", "interior", "hologram", "entity", "scene", "bootstrap", "tick", "debug", "companion", "npc"), args[0]);
        }
        String root = args[0].toLowerCase(Locale.ROOT);
        String[] subArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
        return switch (root) {
            case "help" -> helpCommand.onTabComplete(sender, command, alias, subArgs);
            case "ui" -> uiCommand.onTabComplete(sender, command, alias, subArgs);
            case "data" -> dataCommand.onTabComplete(sender, command, alias, subArgs);
            case "castle" -> castleCommand.onTabComplete(sender, command, alias, subArgs);
            case "citizens" -> citizensCommand.onTabComplete(sender, command, alias, subArgs);
            case "troops" -> troopsCommand.onTabComplete(sender, command, alias, subArgs);
            case "resources" -> resourcesCommand.onTabComplete(sender, command, alias, subArgs);
            case "account" -> accountCommand.onTabComplete(sender, command, alias, subArgs);
            case "buildings" -> buildingsCommand.onTabComplete(sender, command, alias, subArgs);
            case "nodes" -> nodesCommand.onTabComplete(sender, command, alias, subArgs);
            case "place" -> placeCommand.onTabComplete(sender, command, alias, subArgs);
            case "interior" -> interiorCommand.onTabComplete(sender, command, alias, subArgs);
            case "hologram" -> hologramCommand.onTabComplete(sender, command, alias, subArgs);
            case "entity", "entities" -> entityCommand.onTabComplete(sender, command, alias, subArgs);
            case "scene" -> sceneCommand.onTabComplete(sender, command, alias, subArgs);
            case "bootstrap" -> bootstrapCommand.onTabComplete(sender, command, alias, subArgs);
            case "tick" -> tickCommand.onTabComplete(sender, command, alias, subArgs);
            case "debug" -> debugCommand.onTabComplete(sender, command, alias, subArgs);
            case "companion", "companions" -> companionCommand.onTabComplete(sender, command, alias, subArgs);
            case "npc" -> npcCommand.onTabComplete(sender, command, alias, subArgs);
            case "building" -> buildingCommand.onTabComplete(sender, command, alias, subArgs);
            default -> List.of();
        };
    }
}
