package org.tavall.minecraft.commands;

import org.tavall.api.minecraft.permissions.PunishResponse;
import org.tavall.minecraft.commands.support.MinecraftVelocityPunishmentCommandSupport;
import org.tavall.minecraft.routing.MinecraftVelocityCommandResult;
import org.tavall.minecraft.commands.source.MinecraftVelocityCommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;

public final class Unban extends MinecraftVelocityPunishmentCommandSupport implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        MinecraftVelocityCommandSource source = commandSource(invocation);
        MinecraftVelocityCommandResult result = execute(source, invocation.alias(), invocation.arguments());
        invocation.source().sendMessage(Component.text(result.message()));
    }

    public MinecraftVelocityCommandResult execute(MinecraftVelocityCommandSource source, String alias, String[] args) {
        if (args.length != 1) {
            return MinecraftVelocityCommandResult.denied("Usage: /unban <player>");
        }
        if (!canExecute(source, "network.unban")) {
            return MinecraftVelocityCommandResult.denied("Missing permission network.unban.");
        }

        String targetName = args[0];
        PunishResponse response = getMinecraftControlCommandClient().submitPunishRequest(
                unbanRequest(source, alias, targetName)
        );

        return new MinecraftVelocityCommandResult(response.success(), response.message());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }
}
