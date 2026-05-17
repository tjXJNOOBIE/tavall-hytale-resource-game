package com.tavall.resourcegame.frontend.minecraft.server;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.Locale;

public final class KingdomCompanionCommand implements CommandExecutor, TabCompleter, IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    private final KingdomCompanionListCommand listCommand = new KingdomCompanionListCommand();
    private final KingdomCompanionGiveCommand giveCommand = new KingdomCompanionGiveCommand();
    private final KingdomCompanionCreateCommand createCommand = new KingdomCompanionCreateCommand();
    private final KingdomCompanionDebugCommand debugCommand = new KingdomCompanionDebugCommand();
    private final KingdomCompanionSetLevelCommand setLevelCommand = new KingdomCompanionSetLevelCommand();
    private final KingdomCompanionXpCommand xpCommand = new KingdomCompanionXpCommand();
    private final KingdomCompanionMoraleCommand moraleCommand = new KingdomCompanionMoraleCommand();
    private final KingdomCompanionBehaviorCommand behaviorCommand = new KingdomCompanionBehaviorCommand();
    private final KingdomCompanionTrainCommand trainCommand = new KingdomCompanionTrainCommand();
    private final KingdomCompanionClaimCommand claimCommand = new KingdomCompanionClaimCommand();
    private final KingdomCompanionCancelCommand cancelCommand = new KingdomCompanionCancelCommand();
    private final KingdomCompanionSkillCommand skillCommand = new KingdomCompanionSkillCommand();
    private final KingdomCompanionSummonCommand summonCommand = new KingdomCompanionSummonCommand();
    private final KingdomCompanionRecallCommand recallCommand = new KingdomCompanionRecallCommand();
    private final KingdomCompanionWallCommand wallCommand = new KingdomCompanionWallCommand();
    private final KingdomCompanionUiCommand uiCommand = new KingdomCompanionUiCommand();
    private final KingdomCompanionProjectionCommand projectionCommand = new KingdomCompanionProjectionCommand();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /kd companion <list|give|create|debug|setlevel|xp|morale|behavior|train|claim|cancel|skill|summon|recall|wall|ui|projection>");
            return true;
        }
        String root = args[0].toLowerCase(Locale.ROOT);
        String[] subArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
        return switch (root) {
            case "list" -> listCommand.onCommand(sender, command, label, subArgs);
            case "give" -> giveCommand.onCommand(sender, command, label, subArgs);
            case "create" -> createCommand.onCommand(sender, command, label, subArgs);
            case "debug" -> debugCommand.onCommand(sender, command, label, subArgs);
            case "setlevel" -> setLevelCommand.onCommand(sender, command, label, subArgs);
            case "xp" -> xpCommand.onCommand(sender, command, label, subArgs);
            case "morale" -> moraleCommand.onCommand(sender, command, label, subArgs);
            case "behavior" -> behaviorCommand.onCommand(sender, command, label, subArgs);
            case "train" -> trainCommand.onCommand(sender, command, label, subArgs);
            case "claim" -> claimCommand.onCommand(sender, command, label, subArgs);
            case "cancel" -> cancelCommand.onCommand(sender, command, label, subArgs);
            case "skill" -> skillCommand.onCommand(sender, command, label, subArgs);
            case "summon" -> summonCommand.onCommand(sender, command, label, subArgs);
            case "recall" -> recallCommand.onCommand(sender, command, label, subArgs);
            case "wall" -> wallCommand.onCommand(sender, command, label, subArgs);
            case "ui" -> uiCommand.onCommand(sender, command, label, subArgs);
            case "projection" -> projectionCommand.onCommand(sender, command, label, subArgs);
            default -> {
                sender.sendMessage("Usage: /kd companion <list|give|create|debug|setlevel|xp|morale|behavior|train|claim|cancel|skill|summon|recall|wall|ui|projection>");
                yield true;
            }
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            return List.of("list", "give", "create", "debug", "setlevel", "xp", "morale", "behavior", "train", "claim", "cancel", "skill", "summon", "recall", "wall", "ui", "projection");
        }
        if (args.length == 1) {
            return KingdomCommandSupport.matching(List.of("list", "give", "create", "debug", "setlevel", "xp", "morale", "behavior", "train", "claim", "cancel", "skill", "summon", "recall", "wall", "ui", "projection"), args[0]);
        }
        String root = args[0].toLowerCase(Locale.ROOT);
        String[] subArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
        return switch (root) {
            case "list" -> listCommand.onTabComplete(sender, command, alias, subArgs);
            case "give" -> giveCommand.onTabComplete(sender, command, alias, subArgs);
            case "create" -> createCommand.onTabComplete(sender, command, alias, subArgs);
            case "debug" -> debugCommand.onTabComplete(sender, command, alias, subArgs);
            case "setlevel" -> setLevelCommand.onTabComplete(sender, command, alias, subArgs);
            case "xp" -> xpCommand.onTabComplete(sender, command, alias, subArgs);
            case "morale" -> moraleCommand.onTabComplete(sender, command, alias, subArgs);
            case "behavior" -> behaviorCommand.onTabComplete(sender, command, alias, subArgs);
            case "train" -> trainCommand.onTabComplete(sender, command, alias, subArgs);
            case "claim" -> claimCommand.onTabComplete(sender, command, alias, subArgs);
            case "cancel" -> cancelCommand.onTabComplete(sender, command, alias, subArgs);
            case "skill" -> skillCommand.onTabComplete(sender, command, alias, subArgs);
            case "summon" -> summonCommand.onTabComplete(sender, command, alias, subArgs);
            case "recall" -> recallCommand.onTabComplete(sender, command, alias, subArgs);
            case "wall" -> wallCommand.onTabComplete(sender, command, alias, subArgs);
            case "ui" -> uiCommand.onTabComplete(sender, command, alias, subArgs);
            case "projection" -> projectionCommand.onTabComplete(sender, command, alias, subArgs);
            default -> List.of();
        };
    }
}
