package org.tavall.minecraft.commands;

import org.tavall.api.minecraft.permissions.PunishRecord;
import org.tavall.api.minecraft.permissions.PunishResponse;
import org.tavall.minecraft.commands.util.MinecraftVelocityPunishmentCommandSupport;
import org.tavall.minecraft.routing.MinecraftVelocityCommandResult;
import org.tavall.minecraft.commands.source.MinecraftVelocityCommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.util.Arrays;

public final class Ban extends MinecraftVelocityPunishmentCommandSupport implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        MinecraftVelocityCommandResult result = execute(commandSource(invocation), invocation.alias(), invocation.arguments());
        invocation.source().sendMessage(Component.text(result.message()));
    }

    public MinecraftVelocityCommandResult execute(MinecraftVelocityCommandSource source, String alias, String[] args) {

        if (args.length < 1) {
            return MinecraftVelocityCommandResult.denied("Usage: /ban <player> [duration] [reason]");
        }
        if (!canExecute(source, "network.ban")) {
            return MinecraftVelocityCommandResult.denied("Missing permission network.ban.");
        }

        String targetName = args[0];
        String durationText = args.length >= 2 ? args[1] : "Permanent";
        String reason = args.length >= 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Not provided";
        if (determineTimedDuration(durationText) == null) {
            return MinecraftVelocityCommandResult.denied("Invalid ban duration format. Use formats like 1m, 1h, 1d, 1w, 1mo, 1yr, p, permanent.");
        }
        PunishResponse response = getMinecraftControlCommandClient().submitPunishRequest(
                banRequest(source, "ban", targetName, durationText, reason)
        );

        if (response.success() && response.activePunishment() != null) {
            resolveOnlineTarget(targetName).ifPresent(player -> disconnectBannedPlayer(player, response.activePunishment()));
        }
        return new MinecraftVelocityCommandResult(response.success(), response.message());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }

    private void disconnectBannedPlayer(Player player, PunishRecord punishment) {
        String reason = punishment.reason().isBlank() ? "Not provided" : punishment.reason();
        player.disconnect(Component.text("You were banned by " + punishment.senderDisplayName() + ". Reason: " + reason + "."));
    }
}
