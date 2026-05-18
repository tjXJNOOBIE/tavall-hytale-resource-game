package org.tavall.minecraft.commands;

import org.tavall.api.minecraft.permissions.PunishRecord;
import org.tavall.api.minecraft.permissions.PunishResponse;
import org.tavall.minecraft.commands.support.MinecraftVelocityPunishmentCommandSupport;
import org.tavall.minecraft.routing.MinecraftVelocityCommandResult;
import org.tavall.minecraft.commands.source.MinecraftVelocityCommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.util.Arrays;
import java.util.Optional;

public final class Kick extends MinecraftVelocityPunishmentCommandSupport implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        MinecraftVelocityCommandResult result = execute(commandSource(invocation), invocation.alias(), invocation.arguments());
        invocation.source().sendMessage(Component.text(result.message()));
    }

    public MinecraftVelocityCommandResult execute(MinecraftVelocityCommandSource source, String alias, String[] args) {
        if (args.length < 1) {
            return MinecraftVelocityCommandResult.denied("Usage: /kick <player> [reason]");
        }
        if (!canExecute(source, "network.kick")) {
            return MinecraftVelocityCommandResult.denied("No permission.");
        }
        String targetName = args[0];
        Optional<Player> onlineTarget = resolveOnlineTarget(targetName);
        if (onlineTarget.isEmpty()) {
            return MinecraftVelocityCommandResult.denied(targetName + " is offline.");
        }
        String reason = args.length >= 2 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Not provided";
        PunishResponse response = getMinecraftControlCommandClient().submitPunishRequest(
                kickRequest(source, alias, targetName, reason)
        );
        if (response.success()) {
            disconnectKickedPlayer(onlineTarget.get(), response.activePunishment());
        }
        return new MinecraftVelocityCommandResult(response.success(), response.message());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }

    private void disconnectKickedPlayer(Player player, PunishRecord punishment) {
        String senderName = punishment == null || punishment.senderDisplayName().isBlank() ? "Console" : punishment.senderDisplayName();
        String reason = punishment == null || punishment.reason().isBlank() ? "Not provided" : punishment.reason();
        player.disconnect(Component.text("You were kicked by " + senderName + ". Reason: " + reason + "."));
    }
}
