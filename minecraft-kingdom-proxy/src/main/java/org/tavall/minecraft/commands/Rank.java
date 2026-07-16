package org.tavall.minecraft.commands;

import com.velocitypowered.api.command.SimpleCommand;
import org.tavall.api.minecraft.backend.rank.RankAccess;
import org.tavall.api.minecraft.backend.rank.RankDefinition;
import org.tavall.api.minecraft.backend.rank.RankPlayerProfile;
import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.dependency.annotations.DelegatesToInterface;
import org.tavall.dependency.composition.IDependencyBundleAccess;
import org.tavall.minecraft.bootstrap.VelocityDependencies;
import org.tavall.minecraft.commands.source.IVelocityCommandSource;
import org.tavall.minecraft.commands.support.VelocityCommandResult;
import org.tavall.minecraft.commands.support.VelocityProxyCommandSupport;

import java.util.Locale;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

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

        if (!canUse(source) && !canManage(source)) {
            return VelocityCommandResult.denied("Missing permission " + getVelocityProxyConfig().commandPermission() + ".");
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
        } catch (IllegalStateException exception) {
            return VelocityCommandResult.denied(exception.getMessage());
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
        if (!canUse(source) && !canManage(source)) {
            return VelocityCommandResult.denied("Missing permission " + getVelocityProxyConfig().commandPermission() + ".");
        }
        RankAccess rankAccess = getRankAccess();
        java.util.List<RankDefinition> definitions = rankAccess.findRankDefinitions();
        StringJoiner joiner = new StringJoiner(", ");
        for (RankDefinition definition : definitions) {
            joiner.add(definition.rankName() + "(" + definition.powerLevel() + ")");
        }
        String message = definitions.isEmpty()
                ? "No ranks are configured."
                : "Loaded " + definitions.size() + " rank definitions. " + joiner;
        return VelocityCommandResult.completed(message);
    }

    private VelocityCommandResult executeInspect(IVelocityCommandSource source, String alias, String[] args) {
        if (args.length < 2) {
            return usage(alias);
        }
        if (!canUse(source) && !canManage(source)) {
            return VelocityCommandResult.denied("Missing permission " + getVelocityProxyConfig().commandPermission() + ".");
        }
        String targetName = args[1];
        Optional<RankPlayerProfile> profile = resolveTargetProfile(targetName);
        if (profile.isEmpty()) {
            return VelocityCommandResult.denied("Player not found: " + targetName);
        }
        return VelocityCommandResult.completed(formatSubjectResponse("Inspect", profile.get()));
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
        RankAccess rankAccess = getRankAccess();
        RankDefinition rankDefinition = resolveRankDefinition(rankName);
        Optional<RankPlayerProfile> profile = resolveTargetProfile(targetName);
        if (profile.isEmpty()) {
            return VelocityCommandResult.denied("Player not found: " + targetName);
        }

        RankPlayerProfile current = profile.get();
        if (isUuid(targetName)) {
            rankAccess.setRank(UUID.fromString(targetName), rankName);
        } else if (rankAccess.playerExistsByUsername(targetName)) {
            rankAccess.setRankFromUsername(targetName, rankName);
        } else {
            rankAccess.setRank(UUID.fromString(current.platformAccountId()), rankName);
        }

        RankPlayerProfile updated = resolveTargetProfile(targetName).orElse(current);
        return VelocityCommandResult.completed(formatSubjectResponse("Updated", updated.withRank(rankDefinition, java.time.Instant.now())));
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
        RankAccess rankAccess = getRankAccess();
        RankDefinition fallbackDefinition = resolveRankDefinition(fallbackRankName);
        Optional<RankPlayerProfile> profile = resolveTargetProfile(targetName);
        if (profile.isEmpty()) {
            return VelocityCommandResult.denied("Player not found: " + targetName);
        }

        RankPlayerProfile current = profile.get();
        if (isUuid(targetName)) {
            rankAccess.revokeRank(UUID.fromString(targetName), fallbackRankName);
        } else if (rankAccess.playerExistsByUsername(targetName)) {
            rankAccess.setRankFromUsername(targetName, fallbackRankName);
        } else {
            rankAccess.revokeRank(UUID.fromString(current.platformAccountId()), fallbackRankName);
        }

        RankPlayerProfile updated = resolveTargetProfile(targetName).orElse(current);
        return VelocityCommandResult.completed(formatSubjectResponse("Removed", updated.withRank(fallbackDefinition, java.time.Instant.now())));
    }

    private VelocityCommandResult usage(String alias) {
        return VelocityCommandResult.denied("Usage: /" + alias + " <list|inspect|set|remove> ...");
    }

    private Optional<RankPlayerProfile> resolveTargetProfile(String targetName) {
        RankAccess rankAccess = getRankAccess();
        Optional<RankPlayerProfile> byName = rankAccess.findPlayerProfileByDisplayName(targetName);
        if (byName.isPresent()) {
            return byName;
        }
        if (isUuid(targetName)) {
            return rankAccess.findPlayerProfile(targetName);
        }
        return resolveOnlineTarget(targetName)
                .flatMap(player -> rankAccess.findPlayerProfile(player.getUniqueId().toString()))
                .or(() -> rankAccess.findPlayerProfileByUsername(targetName));
    }

    private RankDefinition resolveRankDefinition(String rankName) {
        return getRankAccess().findRankDefinition(rankName)
                .orElseThrow(() -> new IllegalStateException("Rank does not exist: " + rankName));
    }

    private boolean isUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private String formatSubjectResponse(String action, RankPlayerProfile profile) {
        return action + " " + profile.displayName() + " -> " + profile.rankName()
                + " power=" + profile.powerLevel()
                + " permissions=" + profile.permissions();
    }
}
