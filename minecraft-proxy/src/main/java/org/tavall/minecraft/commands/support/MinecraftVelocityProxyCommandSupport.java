package org.tavall.minecraft.commands.support;

import org.tavall.api.minecraft.permissions.UniversalPermissionPolicy;
import org.tavall.api.minecraft.permissions.UniversalPermissionSubject;
import org.tavall.minecraft.commands.source.ConsoleVelocityCommandSource;
import org.tavall.minecraft.commands.source.GenericVelocityCommandSource;
import org.tavall.minecraft.runtime.IMinecraftFrontendDomain;
import org.tavall.minecraft.runtime.IMinecraftVelocityProxyServer;
import org.tavall.minecraft.commands.source.MinecraftVelocityCommandSource;
import org.tavall.minecraft.commands.source.PlayerVelocityCommandSource;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public abstract class MinecraftVelocityProxyCommandSupport implements IMinecraftFrontendDomain, IDependencyInjectableConcrete {
    protected MinecraftVelocityCommandSource commandSource(SimpleCommand.Invocation invocation) {
        if (invocation.source() instanceof Player player) {
            return new PlayerVelocityCommandSource(player);
        }
        if (invocation.source() instanceof ConsoleCommandSource console) {
            return new ConsoleVelocityCommandSource(console);
        }
        return new GenericVelocityCommandSource(invocation.source());
    }

    protected boolean canManage(MinecraftVelocityCommandSource source) {
        UniversalPermissionSubject subject = getMinecraftVelocityPermissionResolver().resolveSubject(source);
        return new UniversalPermissionPolicy().canExecuteAdminCommand(subject);
    }

    protected boolean canExecute(MinecraftVelocityCommandSource source, String permission) {
        return canManage(source) || source.hasPermission(permission);
    }

    protected boolean canUse(MinecraftVelocityCommandSource source) {
        UniversalPermissionSubject subject = getMinecraftVelocityPermissionResolver().resolveSubject(source);
        return new UniversalPermissionPolicy().canExecuteUserCommand(subject);
    }

    protected Map<String, String> baseContext(MinecraftVelocityCommandSource source, String alias) {
        Map<String, String> context = new LinkedHashMap<>(source.metadata());
        context.put("sourceType", source.sourceType());
        context.put("proxy", getMinecraftProxyConfig().serverId());
        context.put("alias", alias);
        context.put("surfaceIdentity", "VELOCITY_PROXY");
        return context;
    }

    protected String resolveTargetAccountId(String targetName) {
        Optional<Player> player = resolveProxyServer().flatMap(proxyServer -> proxyServer.getPlayer(targetName));
        if (player.isPresent()) {
            return player.get().getUniqueId().toString();
        }
        return targetName;
    }

    protected String resolveTargetDisplayName(String targetName) {
        Optional<Player> player = resolveProxyServer().flatMap(proxyServer -> proxyServer.getPlayer(targetName));
        return player.map(Player::getUsername).orElse(targetName);
    }

    protected Optional<Player> resolveOnlineTarget(String targetName) {
        return resolveProxyServer().flatMap(proxyServer -> proxyServer.getPlayer(targetName));
    }

    protected String safeMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message;
    }

    private Optional<IMinecraftVelocityProxyServer> resolveProxyServer() {
        return DependencyLoaderAccess.findOptionalInstance(IMinecraftVelocityProxyServer.class);
    }
}
