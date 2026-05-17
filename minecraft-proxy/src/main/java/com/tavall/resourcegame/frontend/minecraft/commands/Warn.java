package com.tavall.resourcegame.frontend.minecraft.commands;

import com.tavall.resourcegame.api.internal.permissions.PunishResponse;
import com.tavall.resourcegame.frontend.minecraft.MinecraftVelocityCommandResult;
import com.tavall.resourcegame.frontend.minecraft.MinecraftVelocityCommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;

import java.util.Arrays;

public final class Warn extends MinecraftVelocityPunishmentCommandSupport implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        MinecraftVelocityCommandSource source = commandSource(invocation);
        MinecraftVelocityCommandResult result = execute(source, invocation.alias(), invocation.arguments());
        invocation.source().sendMessage(Component.text(result.message()));
    }

    public MinecraftVelocityCommandResult execute(MinecraftVelocityCommandSource source, String alias, String[] args) {
        if (args.length < 1) {
            return MinecraftVelocityCommandResult.denied("Usage: /warn <player> [reason]");
        }
        if (!canExecute(source, "network.warn")) {
            return MinecraftVelocityCommandResult.denied("No permission.");
        }

        String targetName = args[0];
        String reason = args.length >= 2 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Not provided";
        PunishResponse response = getMinecraftControlCommandClient().submitPunishRequest(
                warnRequest(source, alias, targetName, reason)
        );

        return new MinecraftVelocityCommandResult(response.success(), response.message());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }
}
