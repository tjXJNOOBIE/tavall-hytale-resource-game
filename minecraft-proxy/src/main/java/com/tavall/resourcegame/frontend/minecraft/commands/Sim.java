package org.tavall.minecraft.commands;

import org.tavall.minecraft.routing.MinecraftVelocityCommandResult;
import org.tavall.minecraft.commands.util.MinecraftVelocityProxyCommandSupport;
import org.tavall.minecraft.commands.source.MinecraftVelocityCommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;

public final class Sim extends MinecraftVelocityProxyCommandSupport implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        MinecraftVelocityCommandResult result = execute(commandSource(invocation), invocation.alias(), invocation.arguments());
        invocation.source().sendMessage(Component.text(result.message()));
    }

    public MinecraftVelocityCommandResult execute(MinecraftVelocityCommandSource source, String alias, String[] args) {
        return new MinecraftVelocityCommandResult(
                true,
                "Simulation tooling is not enabled on this proxy."
        );
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }
}
