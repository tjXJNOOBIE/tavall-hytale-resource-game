package com.tavall.resourcegame.frontend.minecraft.commands;

import com.tavall.resourcegame.api.internal.permissions.PunishResponse;
import com.tavall.resourcegame.frontend.minecraft.commands.util.MinecraftVelocityPunishmentCommandSupport;
import com.tavall.resourcegame.frontend.minecraft.routing.MinecraftVelocityCommandResult;
import com.tavall.resourcegame.frontend.minecraft.commands.source.MinecraftVelocityCommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;

public final class Unwarn extends MinecraftVelocityPunishmentCommandSupport implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        MinecraftVelocityCommandResult result = execute(commandSource(invocation), invocation.alias(), invocation.arguments());
        invocation.source().sendMessage(Component.text(result.message()));
    }

    public MinecraftVelocityCommandResult execute(MinecraftVelocityCommandSource source, String alias, String[] args) {
        if (args.length != 1) {
            return MinecraftVelocityCommandResult.denied("Usage: /unwarn <player>");
        }
        if (!canExecute(source, "network.unwarn")) {
            return MinecraftVelocityCommandResult.denied("Missing permission network.unwarn.");
        }
        PunishResponse response = getMinecraftControlCommandClient().submitPunishRequest(
                unwarnRequest(source, alias, args[0])
        );
        return new MinecraftVelocityCommandResult(response.success(), response.message());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }
}
