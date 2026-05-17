package com.tavall.resourcegame.frontend.minecraft.commands;

import com.tavall.resourcegame.api.internal.permissions.PunishResponse;
import com.tavall.resourcegame.frontend.minecraft.routing.MinecraftVelocityCommandResult;
import com.tavall.resourcegame.frontend.minecraft.commands.source.MinecraftVelocityCommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;

import java.util.Arrays;

public final class Mute extends MinecraftVelocityPunishmentCommandSupport implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        MinecraftVelocityCommandResult result = execute(commandSource(invocation), invocation.alias(), invocation.arguments());
        invocation.source().sendMessage(Component.text(result.message()));
    }

    public MinecraftVelocityCommandResult execute(MinecraftVelocityCommandSource source, String alias, String[] args) {
        if (args.length < 1) {
            return MinecraftVelocityCommandResult.denied("Usage: /mute <player> [duration] [reason]");
        }
        if (!canExecute(source, "network.mute")) {
            return MinecraftVelocityCommandResult.denied("Missing permission network.mute.");
        }
        String targetName = args[0];
        String durationText = args.length >= 2 ? args[1] : "Permanent";
        String reason = args.length >= 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Not provided";
        if (determineTimedDuration(durationText) == null) {
            return MinecraftVelocityCommandResult.denied("Invalid mute duration format. Use formats like 1m, 1h, 1d, 1w, 1mo, 1yr, p, permanent.");
        }
        PunishResponse response = getMinecraftControlCommandClient().submitPunishRequest(
                muteRequest(source, alias, targetName, durationText, reason)
        );
        return new MinecraftVelocityCommandResult(response.success(), response.message());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }
}
