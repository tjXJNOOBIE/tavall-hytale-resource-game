package org.tavall.minecraft.commands.support;

import org.tavall.api.minecraft.permissions.UniversalPermissionPolicy;
import org.tavall.api.minecraft.permissions.UniversalPermissionSubject;
import org.tavall.api.minecraft.cache.IKingdomSemanticCacheGateway;
import org.tavall.api.minecraft.backend.rank.RankAccess;
import org.tavall.minecraft.bootstrap.VelocityDependencies;
import org.tavall.minecraft.bootstrap.VelocityProxyConfig;
import org.tavall.minecraft.commands.source.ConsoleVelocityCommandSource;
import org.tavall.minecraft.commands.source.GenericVelocityCommandSource;
import org.tavall.minecraft.commands.source.IVelocityCommandSource;
import org.tavall.minecraft.commands.source.PlayerVelocityCommandSource;
import org.tavall.minecraft.permissions.IVelocityCommandPermissionHandler;
import org.tavall.minecraft.permissions.IVelocityPermissionResolver;
import org.tavall.dependency.IDependencyInjectableConcrete;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public abstract class VelocityProxyCommandSupport implements IDependencyInjectableConcrete {
    protected IVelocityCommandSource commandSource(SimpleCommand.Invocation invocation) {
        return commandSource(invocation.source());
    }

    protected IVelocityCommandSource commandSource(CommandSource source) {
        if (source instanceof Player player) {
            return new PlayerVelocityCommandSource(player);
        }
        if (source instanceof ConsoleCommandSource console) {
            return new ConsoleVelocityCommandSource(console);
        }
        return new GenericVelocityCommandSource(source);
    }

    protected boolean canManage(IVelocityCommandSource source) {
        UniversalPermissionSubject subject = getVelocityPermissionResolver().resolveSubject(source);
        return new UniversalPermissionPolicy().canExecuteAdminCommand(subject);
    }

    protected boolean canExecute(IVelocityCommandSource source, String permission) {
        return canManage(source) || source.hasPermission(permission);
    }

    protected boolean canUse(IVelocityCommandSource source) {
        UniversalPermissionSubject subject = getVelocityPermissionResolver().resolveSubject(source);
        return new UniversalPermissionPolicy().canExecuteUserCommand(subject);
    }

    protected Map<String, String> baseContext(IVelocityCommandSource source, String alias) {
        Map<String, String> context = new LinkedHashMap<>(source.metadata());
        context.put("sourceType", source.sourceType());
        context.put("proxy", getVelocityProxyConfig().serverId());
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

    protected VelocityDependencies velocityDependencies() {
        return org.tavall.dependency.DependencyLoaderAccess.findOptionalInstance(VelocityDependencies.class)
                .orElseThrow(() -> new IllegalStateException("VelocityDependencies are not registered."));
    }

    protected VelocityProxyConfig getVelocityProxyConfig() {
        return velocityDependencies().velocityProxyConfig();
    }

    protected IKingdomSemanticCacheGateway getKingdomCacheGateway() {
        return velocityDependencies().kingdomSemanticCacheGateway();
    }

    protected IVelocityPermissionResolver getVelocityPermissionResolver() {
        return velocityDependencies().velocityPermissionResolver();
    }

    protected IVelocityCommandPermissionHandler getVelocityCommandPermissionHandler() {
        return velocityDependencies().velocityCommandPermissionHandler();
    }

    protected org.tavall.minecraft.commands.ISim getSimCommand() {
        return velocityDependencies().simCommand();
    }

    protected org.tavall.minecraft.commands.IGuild getGuildCommand() {
        return velocityDependencies().guildCommand();
    }

    protected org.tavall.minecraft.commands.IRank getRankCommand() {
        return velocityDependencies().rankCommand();
    }

    protected RankAccess getRankAccess() {
        return velocityDependencies().rankAccess();
    }

    protected Optional<ProxyServer> resolveProxyServer() {
        return org.tavall.dependency.DependencyLoaderAccess.findOptionalInstance(ProxyServer.class);
    }

    protected Component formatCommandResult(VelocityCommandResult result) {
        NamedTextColor color = result.success() ? NamedTextColor.GREEN : NamedTextColor.RED;
        if (result.message().startsWith("Usage:")) {
            color = NamedTextColor.GOLD;
        } else if (result.message().startsWith("COMPLETED:")) {
            color = NamedTextColor.GREEN;
        } else if (result.message().startsWith("FAILED:")) {
            color = NamedTextColor.RED;
        }
        return Component.text(result.message(), color);
    }

    protected void sendCommandResult(CommandSource source, VelocityCommandResult result) {
        source.sendMessage(formatCommandResult(result));
    }
}



