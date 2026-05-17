package com.tavall.resourcegame.frontend.minecraft.commands;

import com.tavall.resourcegame.api.internal.permissions.PunishResponse;
import com.tavall.resourcegame.frontend.minecraft.routing.MinecraftVelocityCommandResult;
import com.tavall.resourcegame.frontend.minecraft.commands.source.MinecraftVelocityCommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;

public final class Unmute extends MinecraftVelocityPunishmentCommandSupport implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        MinecraftVelocityCommandResult result = execute(commandSource(invocation), invocation.alias(), invocation.arguments());
        invocation.source().sendMessage(Component.text(result.message()));
    }

    public MinecraftVelocityCommandResult execute(MinecraftVelocityCommandSource source, String alias, String[] args) {
        if (args.length != 1) {
            return MinecraftVelocityCommandResult.denied("Usage: /unmute <player>");
        }
        if (!canExecute(source, "network.unmute")) {
            return MinecraftVelocityCommandResult.denied("Missing permission network.unmute.");
        }
        PunishResponse response = getMinecraftControlCommandClient().submitPunishRequest(
                unmuteRequest(source, alias, args[0])
        );
        return new MinecraftVelocityCommandResult(response.success(), response.message());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }
}
