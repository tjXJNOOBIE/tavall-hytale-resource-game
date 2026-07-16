package org.tavall.minecraft.commands;

import org.tavall.minecraft.commands.source.ConsoleVelocityCommandSource;
import org.tavall.minecraft.commands.source.GenericVelocityCommandSource;
import org.tavall.minecraft.commands.source.MinecraftVelocityCommandSource;
import org.tavall.minecraft.commands.source.PlayerVelocityCommandSource;
import org.tavall.minecraft.routing.MinecraftVelocityCommandResult;
import org.tavall.minecraft.permissions.IMinecraftFrontendPermissionDependencyAccess;
import org.tavall.minecraft.routing.IMinecraftFrontendRoutingDependencyAccess;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import net.kyori.adventure.text.Component;

public final class MinecraftVelocitySimpleCommand implements SimpleCommand, IMinecraftFrontendPermissionDependencyAccess, IMinecraftFrontendRoutingDependencyAccess, IDependencyInjectableConcrete {
    @Override
    public void execute(Invocation invocation) {
        MinecraftVelocityCommandResult result = getMinecraftVelocityCommandExecutionHandler().execute(
                commandSource(invocation),
                invocation.alias(),
                invocation.arguments()
        );
        invocation.source().sendMessage(Component.text(result.message()));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return getMinecraftVelocityCommandPermissionHandler().canExecute(commandSource(invocation), invocation.alias(), invocation.arguments());
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
