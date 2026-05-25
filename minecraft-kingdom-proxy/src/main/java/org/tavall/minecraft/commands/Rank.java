package org.tavall.minecraft.commands;

import com.velocitypowered.api.command.SimpleCommand;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;
import org.tavall.api.minecraft.permissions.RankRequest;
import org.tavall.api.minecraft.permissions.RankResponse;
import org.tavall.api.minecraft.permissions.RankSubject;
import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.dependency.annotations.DelegatesToInterface;
import org.tavall.dependency.composition.IDependencyBundleAccess;
import org.tavall.minecraft.bootstrap.VelocityDependencies;
import org.tavall.minecraft.commands.source.IVelocityCommandSource;
import org.tavall.minecraft.commands.support.VelocityCommandResult;
import org.tavall.minecraft.commands.support.VelocityProxyCommandSupport;
import org.tavall.minecraft.permissions.IRankControlBridgeClient;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

@DelegatesToInterface(getLinkedInterface = IRank.class)
public final class Rank extends VelocityProxyCommandSupport implements IRank, IDependencyInjectableConcrete, IDependencyBundleAccess<VelocityDependencies> {
    @Override
    public VelocityDependencies dependencies() {
        return getDependencies();
    }

    @Override
    public void execute(Invocation invocation) {
        VelocityCommandResult result = execute(commandSource(invocation), invocation.alias(), invocation.arguments());
        sendCommandResult(invocation.source(), result);
    }

    @Override
    public VelocityCommandResult execute(IVelocityCommandSource source, String alias, String[] args) {
        if (args.length == 0) {
            return usage(alias);
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        try {
            return switch (subcommand) {
                case "list" -> executeList(source, alias);
                case "inspect" -> executeInspect(source, alias, args);
                case "set" -> executeSet(source, alias, args);
                case "remove" -> executeRemove(source, alias, args);
                default -> usage(alias);
            };
        } catch (RuntimeException exception) {
            return VelocityCommandResult.denied(safeMessage(exception));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        IVelocityCommandSource source = commandSource(invocation);
        return canUse(source) || canManage(source);
    }

    private VelocityCommandResult executeList(IVelocityCommandSource source, String alias) {
        if (!canUse(source)) {
            return VelocityCommandResult.denied("Missing permission " + getVelocityProxyConfig().commandPermission() + ".");
        }
        RankResponse response = getRankControlBridgeClient().submitRankRequest(
                RankRequest.list(
                        requestId(alias, source, "list"),
                        ResourceGameFrontendPlatform.MINECRAFT,
                        source.platformAccountId(),
                        source.platformDisplayName(),
                        baseContext(source, alias),
                        Instant.now().toEpochMilli()
                )
        );
        return response.success()
                ? VelocityCommandResult.completed(formatListResponse(response))
                : VelocityCommandResult.denied(response.message());
    }

    private VelocityCommandResult executeInspect(IVelocityCommandSource source, String alias, String[] args) {
        if (args.length < 2) {
            return usage(alias);
        }
        if (!canUse(source)) {
            return VelocityCommandResult.denied("Missing permission " + getVelocityProxyConfig().commandPermission() + ".");
        }
        String targetName = args[1];
        RankResponse response = getRankControlBridgeClient().submitRankRequest(
                RankRequest.inspect(
                        requestId(alias, source, "inspect", targetName),
                        ResourceGameFrontendPlatform.MINECRAFT,
                        source.platformAccountId(),
                        source.platformDisplayName(),
                        resolveTargetAccountId(targetName),
                        resolveTargetDisplayName(targetName),
                        baseContext(source, alias),
                        Instant.now().toEpochMilli()
                )
        );
        return response.success()
                ? VelocityCommandResult.completed(formatSubjectResponse("Inspect", response.subject()))
                : VelocityCommandResult.denied(response.message());
    }

    private VelocityCommandResult executeSet(IVelocityCommandSource source, String alias, String[] args) {
        if (args.length < 3) {
            return usage(alias);
        }
        if (!canManage(source)) {
            return VelocityCommandResult.denied("Missing permission " + getVelocityProxyConfig().adminPermission() + ".");
        }
        String targetName = args[1];
        String rankName = args[2];
        RankResponse response = getRankControlBridgeClient().submitRankRequest(
                RankRequest.setRank(
                        requestId(alias, source, "set", targetName, rankName),
                        ResourceGameFrontendPlatform.MINECRAFT,
                        source.platformAccountId(),
                        source.platformDisplayName(),
                        resolveTargetAccountId(targetName),
                        resolveTargetDisplayName(targetName),
                        rankName,
                        baseContext(source, alias),
                        Instant.now().toEpochMilli()
                )
        );
        return response.success()
                ? VelocityCommandResult.completed(formatSubjectResponse("Updated", response.subject()))
                : VelocityCommandResult.denied(response.message());
    }

    private VelocityCommandResult executeRemove(IVelocityCommandSource source, String alias, String[] args) {
        if (args.length < 2) {
            return usage(alias);
        }
        if (!canManage(source)) {
            return VelocityCommandResult.denied("Missing permission " + getVelocityProxyConfig().adminPermission() + ".");
        }
        String targetName = args[1];
        String fallbackRankName = args.length >= 3 ? args[2] : "Member";
        RankResponse response = getRankControlBridgeClient().submitRankRequest(
                RankRequest.removeRank(
                        requestId(alias, source, "remove", targetName, fallbackRankName),
                        ResourceGameFrontendPlatform.MINECRAFT,
                        source.platformAccountId(),
                        source.platformDisplayName(),
                        resolveTargetAccountId(targetName),
                        resolveTargetDisplayName(targetName),
                        fallbackRankName,
                        baseContext(source, alias),
                        Instant.now().toEpochMilli()
                )
        );
        return response.success()
                ? VelocityCommandResult.completed(formatSubjectResponse("Removed", response.subject()))
                : VelocityCommandResult.denied(response.message());
    }

    private VelocityCommandResult usage(String alias) {
        return VelocityCommandResult.denied("Usage: /" + alias + " <list|inspect|set|remove> ...");
    }

    private String requestId(String alias, IVelocityCommandSource source, String... suffixes) {
        StringJoiner joiner = new StringJoiner(":");
        joiner.add(alias);
        joiner.add(source.sourceType());
        joiner.add(source.platformAccountId());
        for (String suffix : suffixes) {
            joiner.add(suffix);
        }
        joiner.add(String.valueOf(System.nanoTime()));
        return joiner.toString();
    }

    private String formatListResponse(RankResponse response) {
        if (response.subjects().isEmpty()) {
            return response.message();
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (RankSubject subject : response.subjects()) {
            joiner.add(subject.rankName() + "(" + subject.powerLevel() + ")");
        }
        return response.message() + " " + joiner;
    }

    private String formatSubjectResponse(String action, RankSubject subject) {
        if (subject == null) {
            return action + " completed.";
        }
        return action + " " + subject.displayName() + " -> " + subject.rankName()
                + " power=" + subject.powerLevel()
                + " permissions=" + subject.permissions();
    }

    private IRankControlBridgeClient getRankControlBridgeClient() {
        return dependencies().rankControlBridgeClient();
    }
}
