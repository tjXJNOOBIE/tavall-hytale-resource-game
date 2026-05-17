package com.tavall.resourcegame.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.dependency.composition.domains.IResourceGameDomain;
import com.tavall.resourcegame.domain.CustomEntitySpawnRole;

import java.util.List;
import java.util.Locale;

/**
 * Handles `/kd entity ...` debug spawns for custom NPC interaction anchors.
 */
public final class KingdomEntityCommandSupport implements IResourceGameDomain, IDependencyInjectableConcrete {
    public void handle(CommandContext context, Player player, List<String> tokens) {
        if (tokens.size() < 2) {
            sendUsage(context);
            return;
        }
        String action = tokens.get(1).toLowerCase(Locale.ROOT);
        switch (action) {
            case "spawn" -> handleSpawn(context, player, tokens);
            case "clear" -> getCustomEntitySpawnService().clear(player);
            case "list" -> context.sendMessage(Message.raw("Entity roles: " + CustomEntitySpawnRole.commandChoices()).color("yellow"));
            default -> sendUsage(context);
        }
    }

    private void handleSpawn(CommandContext context, Player player, List<String> tokens) {
        if (tokens.size() < 3) {
            sendUsage(context);
            return;
        }
        getCustomEntitySpawnService().spawn(player, tokens.get(2));
    }

    private void sendUsage(CommandContext context) {
        context.sendMessage(Message.raw("Usage: /kd entity spawn <" + CustomEntitySpawnRole.commandChoices() + "> | clear | list").color("yellow"));
    }
}
