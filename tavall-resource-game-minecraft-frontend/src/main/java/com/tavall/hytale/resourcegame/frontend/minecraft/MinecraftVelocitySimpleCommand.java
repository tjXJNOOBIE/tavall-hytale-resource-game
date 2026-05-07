package com.tavall.hytale.resourcegame.frontend.minecraft;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

public final class MinecraftVelocitySimpleCommand implements SimpleCommand {
    private final MinecraftVelocityCommandExecutionHandler executionHandler;
    private final MinecraftVelocityCommandPermissionHandler permissionHandler;

    public MinecraftVelocitySimpleCommand(
            MinecraftVelocityCommandExecutionHandler executionHandler,
            MinecraftVelocityCommandPermissionHandler permissionHandler
    ) {
        this.executionHandler = executionHandler;
        this.permissionHandler = permissionHandler;
    }

    @Override
    public void execute(Invocation invocation) {
        MinecraftVelocityCommandResult result = executionHandler.execute(
                commandSource(invocation),
                invocation.alias(),
                invocation.arguments()
        );
        invocation.source().sendMessage(Component.text(result.message()));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return permissionHandler.canExecute(commandSource(invocation), invocation.alias(), invocation.arguments());
    }

    private MinecraftVelocityCommandSource commandSource(Invocation invocation) {
        if (invocation.source() instanceof Player player) {
            return new PlayerVelocityCommandSource(player);
        }
        if (invocation.source() instanceof ConsoleCommandSource console) {
            return new ConsoleVelocityCommandSource(console);
        }
        return new GenericVelocityCommandSource(invocation.source());
    }
}
