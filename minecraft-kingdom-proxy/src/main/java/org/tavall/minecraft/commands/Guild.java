package org.tavall.minecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import org.tavall.api.minecraft.guild.GuildBankTransaction;
import org.tavall.api.minecraft.guild.GuildChatMessage;
import org.tavall.api.minecraft.guild.GuildContributionEvent;
import org.tavall.api.minecraft.guild.GuildLeaderboardRow;
import org.tavall.api.minecraft.guild.GuildLeaderboardSnapshot;
import org.tavall.api.minecraft.guild.GuildMetaData;
import org.tavall.api.minecraft.guild.GuildPermission;
import org.tavall.api.minecraft.guild.GuildRank;
import org.tavall.api.minecraft.guild.GuildRole;
import org.tavall.api.minecraft.guild.IGuildBankHandler;
import org.tavall.api.minecraft.guild.IGuildChatHandler;
import org.tavall.api.minecraft.guild.IGuildContributionHandler;
import org.tavall.api.minecraft.guild.IGuildCreationHandler;
import org.tavall.api.minecraft.guild.IGuildLeaderboardHandler;
import org.tavall.api.minecraft.guild.IGuildMembershipHandler;
import org.tavall.api.minecraft.guild.IGuildPlayerDirectory;
import org.tavall.api.minecraft.guild.IGuildRoleHandler;
import org.tavall.api.minecraft.guild.IGuildStateStore;
import org.tavall.api.minecraft.guild.GuildResourceType;
import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.dependency.annotations.DelegatesToInterface;
import org.tavall.dependency.composition.IDependencyBundleAccess;
import org.tavall.minecraft.commands.source.IMinecraftVelocityCommandSource;
import org.tavall.minecraft.commands.support.MinecraftVelocityProxyCommandSupport;
import org.tavall.minecraft.commands.support.VelocityCommandResult;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@DelegatesToInterface(getLinkedInterface = IGuild.class)
public final class Guild extends MinecraftVelocityProxyCommandSupport implements IGuild, IDependencyInjectableConcrete, IDependencyBundleAccess<IGuildCommandDependencies> {
    @Override
    public IGuildCommandDependencies dependencies() {
        return getDependencies();
    }

    private IGuildCreationHandler guildCreationHandler() {
        return dependencies().guildCreationHandler();
    }

    private IGuildStateStore guildStateStore() {
        return dependencies().guildStateStore();
    }

    private IGuildPlayerDirectory guildPlayerDirectory() {
        return dependencies().guildPlayerDirectory();
    }

    private IGuildMembershipHandler guildMembershipHandler() {
        return dependencies().guildMembershipHandler();
    }

    private IGuildRoleHandler guildRoleHandler() {
        return dependencies().guildRoleHandler();
    }

    private IGuildBankHandler guildBankHandler() {
        return dependencies().guildBankHandler();
    }

    private IGuildChatHandler guildChatHandler() {
        return dependencies().guildChatHandler();
    }

    private IGuildContributionHandler guildContributionHandler() {
        return dependencies().guildContributionHandler();
    }

    private IGuildLeaderboardHandler guildLeaderboardHandler() {
        return dependencies().guildLeaderboardHandler();
    }

    @Override
    public BrigadierCommand brigadierCommand() {
        return new BrigadierCommand(
                BrigadierCommand.literalArgumentBuilder("guild")
                        .requires(source -> canUse(commandSource(source)))
                        .executes(context -> executeBrigadier(context.getSource()))
                        .then(createGuildBranch())
                        .then(inviteBranch())
                        .then(joinBranch())
                        .then(kickBranch())
                        .then(promoteBranch())
                        .then(roleBranch())
                        .then(bankBranch())
                        .then(chatBranch())
                        .then(contributeBranch())
                        .then(leaderboardBranch())
        );
    }

    public VelocityCommandResult execute(IMinecraftVelocityCommandSource source, String alias, String[] args) {
        if (!canUse(source)) {
            return VelocityCommandResult.denied("Missing permission " + getMinecraftProxyConfig().commandPermission() + ".");
        }
        if (args.length == 0) {
            return usage(alias);
        }

        try {
            return switch (args[0].toLowerCase(Locale.ROOT)) {
                case "create" -> executeCreate(alias, source, args);
                case "invite" -> executeInvite(source, args);
                case "join" -> executeJoin(source, args);
                case "kick" -> executeKick(source, args);
                case "promote" -> executePromote(source, args);
                case "role" -> executeRole(source, args);
                case "bank" -> executeBank(source, args);
                case "chat" -> executeChat(source, args);
                case "contribute" -> executeContribute(source, args);
                case "leaderboard" -> executeLeaderboard(alias, source, args);
                default -> VelocityCommandResult.denied("Unknown guild subcommand: " + args[0] + ". Usage: /guild <create|invite|join|kick|promote|role|bank|chat|contribute|leaderboard> ...");
            };
        } catch (RuntimeException exception) {
            return VelocityCommandResult.denied(safeMessage(exception));
        }
    }

    private LiteralArgumentBuilder<CommandSource> createGuildBranch() {
        return BrigadierCommand.literalArgumentBuilder("create")
                .executes(context -> executeBrigadier(context.getSource(), "create"))
                .then(BrigadierCommand.requiredArgumentBuilder("name", StringArgumentType.word())
                        .then(BrigadierCommand.requiredArgumentBuilder("tag", StringArgumentType.word())
                                .executes(context -> executeBrigadier(
                                        context.getSource(),
                                        "create",
                                        stringArg(context, "name"),
                                        stringArg(context, "tag")
                                ))
                                .then(BrigadierCommand.requiredArgumentBuilder("motto", StringArgumentType.greedyString())
                                        .executes(context -> executeBrigadier(
                                                context.getSource(),
                                                "create",
                                                stringArg(context, "name"),
                                                stringArg(context, "tag"),
                                                stringArg(context, "motto")
                                        )))));
    }

    private LiteralArgumentBuilder<CommandSource> inviteBranch() {
        return BrigadierCommand.literalArgumentBuilder("invite")
                .executes(context -> executeBrigadier(context.getSource(), "invite"))
                .then(BrigadierCommand.requiredArgumentBuilder("target", StringArgumentType.word())
                        .suggests((context, builder) -> suggestInviteTargets(builder))
                        .executes(context -> executeBrigadier(
                                context.getSource(),
                                "invite",
                                stringArg(context, "target")
                        )));
    }

    private LiteralArgumentBuilder<CommandSource> joinBranch() {
        return BrigadierCommand.literalArgumentBuilder("join")
                .executes(context -> executeBrigadier(context.getSource(), "join"))
                .then(BrigadierCommand.requiredArgumentBuilder("guildName", StringArgumentType.word())
                        .suggests((context, builder) -> suggestPublicGuilds(builder))
                        .executes(context -> executeBrigadier(
                                context.getSource(),
                                "join",
                                stringArg(context, "guildName")
                        )));
    }

    private LiteralArgumentBuilder<CommandSource> kickBranch() {
        return BrigadierCommand.literalArgumentBuilder("kick")
                .executes(context -> executeBrigadier(context.getSource(), "kick"))
                .then(BrigadierCommand.requiredArgumentBuilder("target", StringArgumentType.word())
                        .suggests((context, builder) -> suggestOnlinePlayers(builder))
                        .executes(context -> executeBrigadier(
                                context.getSource(),
                                "kick",
                                stringArg(context, "target")
                        )));
    }

    private LiteralArgumentBuilder<CommandSource> promoteBranch() {
        return BrigadierCommand.literalArgumentBuilder("promote")
                .executes(context -> executeBrigadier(context.getSource(), "promote"))
                .then(BrigadierCommand.requiredArgumentBuilder("target", StringArgumentType.word())
                        .suggests((context, builder) -> suggestOnlinePlayers(builder))
                        .then(BrigadierCommand.requiredArgumentBuilder("rank", StringArgumentType.word())
                                .suggests((context, builder) -> suggestGuildRanks(builder))
                                .executes(context -> executeBrigadier(
                                        context.getSource(),
                                        "promote",
                                        stringArg(context, "target"),
                                        stringArg(context, "rank")
                                ))));
    }

    private LiteralArgumentBuilder<CommandSource> roleBranch() {
        return BrigadierCommand.literalArgumentBuilder("role")
                .executes(context -> executeBrigadier(context.getSource(), "role"))
                .then(BrigadierCommand.literalArgumentBuilder("assign")
                        .executes(context -> executeBrigadier(context.getSource(), "role", "assign"))
                        .then(BrigadierCommand.requiredArgumentBuilder("target", StringArgumentType.word())
                                .suggests((context, builder) -> suggestOnlinePlayers(builder))
                                .then(BrigadierCommand.requiredArgumentBuilder("role", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestGuildRoles(builder))
                                        .executes(context -> executeBrigadier(
                                                context.getSource(),
                                                "role",
                                                "assign",
                                                stringArg(context, "target"),
                                                stringArg(context, "role")
                                        )))));
    }

    private LiteralArgumentBuilder<CommandSource> bankBranch() {
        return BrigadierCommand.literalArgumentBuilder("bank")
                .executes(context -> executeBrigadier(context.getSource(), "bank"))
                .then(bankActionBranch("deposit"))
                .then(bankActionBranch("withdraw"));
    }

    private LiteralArgumentBuilder<CommandSource> bankActionBranch(String action) {
        LiteralArgumentBuilder<CommandSource> actionBranch = BrigadierCommand.literalArgumentBuilder(action)
                .executes(context -> executeBrigadier(context.getSource(), "bank", action));

        actionBranch.then(
                BrigadierCommand.requiredArgumentBuilder("resource", StringArgumentType.word())
                        .suggests((context, builder) -> suggestBankResources(builder))
                        .then(
                                BrigadierCommand.requiredArgumentBuilder("amount", LongArgumentType.longArg(1L))
                                        .executes(context -> executeBrigadier(
                                                context.getSource(),
                                                "bank",
                                                action,
                                                stringArg(context, "resource"),
                                                String.valueOf(longArg(context, "amount"))
                                        ))
                                        .then(
                                                BrigadierCommand.requiredArgumentBuilder("reason", StringArgumentType.greedyString())
                                                        .executes(context -> executeBrigadier(
                                                                context.getSource(),
                                                                "bank",
                                                                action,
                                                                stringArg(context, "resource"),
                                                                String.valueOf(longArg(context, "amount")),
                                                                stringArg(context, "reason")
                                                        ))
                                        )
                        )
        );
        return actionBranch;
    }

    private LiteralArgumentBuilder<CommandSource> chatBranch() {
        return BrigadierCommand.literalArgumentBuilder("chat")
                .executes(context -> executeBrigadier(context.getSource(), "chat"))
                .then(BrigadierCommand.requiredArgumentBuilder("channel", StringArgumentType.word())
                        .suggests((context, builder) -> suggestChatChannels(builder))
                        .then(BrigadierCommand.requiredArgumentBuilder("message", StringArgumentType.greedyString())
                                .executes(context -> executeBrigadier(
                                        context.getSource(),
                                        "chat",
                                        stringArg(context, "channel"),
                                        stringArg(context, "message")
                                ))));
    }

    private LiteralArgumentBuilder<CommandSource> contributeBranch() {
        return BrigadierCommand.literalArgumentBuilder("contribute")
                .executes(context -> executeBrigadier(context.getSource(), "contribute"))
                .then(BrigadierCommand.requiredArgumentBuilder("sourceType", StringArgumentType.word())
                        .suggests((context, builder) -> suggestContributionSources(builder))
                        .then(BrigadierCommand.requiredArgumentBuilder("points", LongArgumentType.longArg(0L))
                                .then(BrigadierCommand.requiredArgumentBuilder("coinContribution", LongArgumentType.longArg(0L))
                                        .executes(context -> executeBrigadier(
                                                context.getSource(),
                                                "contribute",
                                                stringArg(context, "sourceType"),
                                                String.valueOf(longArg(context, "points")),
                                                String.valueOf(longArg(context, "coinContribution"))
                                        )))));
    }

    private LiteralArgumentBuilder<CommandSource> leaderboardBranch() {
        return BrigadierCommand.literalArgumentBuilder("leaderboard")
                .executes(context -> executeBrigadier(context.getSource(), "leaderboard"))
                .then(leaderboardMetricBranch("contribution"))
                .then(leaderboardMetricBranch("bank"))
                .then(leaderboardMetricBranch("members"));
    }

    private LiteralArgumentBuilder<CommandSource> leaderboardMetricBranch(String metric) {
        return BrigadierCommand.literalArgumentBuilder(metric)
                .executes(context -> executeBrigadier(context.getSource(), "leaderboard", metric))
                .then(BrigadierCommand.requiredArgumentBuilder("limit", IntegerArgumentType.integer(1))
                        .executes(context -> executeBrigadier(
                                context.getSource(),
                                "leaderboard",
                                metric,
                                String.valueOf(intArg(context, "limit"))
                        )));
    }

    private int executeBrigadier(CommandSource source, String... args) {
        VelocityCommandResult result = execute(commandSource(source), "guild", args);
        sendCommandResult(source, result);
        return result.success() ? Command.SINGLE_SUCCESS : 0;
    }

    private CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestOnlinePlayers(SuggestionsBuilder builder) {
        resolveProxyServer().ifPresent(proxyServer ->
                proxyServer.getAllPlayers().forEach(player -> {
                    builder.suggest(player.getUsername());
                    guildPlayerDirectory().rememberPlayer(player.getUniqueId(), player.getUsername());
                })
        );
        guildPlayerDirectory().knownPlayerNames().forEach(builder::suggest);
        return builder.buildFuture();
    }

    private CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestGuildRanks(SuggestionsBuilder builder) {
        for (GuildRank rank : GuildRank.values()) {
            builder.suggest(rank.name());
        }
        return builder.buildFuture();
    }

    private CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestGuildRoles(SuggestionsBuilder builder) {
        for (GuildRole role : GuildRole.values()) {
            builder.suggest(role.name());
        }
        return builder.buildFuture();
    }

    private CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestChatChannels(SuggestionsBuilder builder) {
        builder.suggest("general");
        builder.suggest("guild");
        builder.suggest("war");
        builder.suggest("trade");
        return builder.buildFuture();
    }

    private CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestContributionSources(SuggestionsBuilder builder) {
        builder.suggest("quest");
        builder.suggest("combat");
        builder.suggest("building");
        builder.suggest("trade");
        builder.suggest("event");
        return builder.buildFuture();
    }

    private VelocityCommandResult executeCreate(String alias, IMinecraftVelocityCommandSource source, String[] args) {
        if (args.length < 3) {
            return usage(alias, "create <name> <tag> <motto>");
        }

        UUID creatorPlayerId = requirePlayerId(source);
        rememberKnownPlayer(creatorPlayerId, source.platformDisplayName());

        String name = args[1];
        String tag = args[2];
        String motto = args.length >= 4 ? joinTail(args, 3) : "";

        GuildMetaData guild = guildCreationHandler().createGuild(creatorPlayerId, name, tag, motto);
        return VelocityCommandResult.completed("Created guild " + guild.name() + " (" + guild.tag() + ").");
    }

    private VelocityCommandResult executeInvite(IMinecraftVelocityCommandSource source, String[] args) {
        if (args.length < 2) {
            return usage("guild", "invite <user|uuid>");
        }

        UUID actorPlayerId = requirePlayerId(source);
        rememberKnownPlayer(actorPlayerId, source.platformDisplayName());

        UUID targetPlayerId = resolveKnownTargetPlayerId(args[1], true)
                .orElseThrow(() -> new IllegalStateException("No stored player profile exists for " + args[1] + ". Ask them to join once or use their UUID."));
        String targetDisplayName = resolveTargetDisplayName(args[1], targetPlayerId);

        GuildMetaData guild = guildMembershipHandler().invitePlayer(actorPlayerId, targetPlayerId);
        return VelocityCommandResult.completed("Recorded an invite for " + targetDisplayName + " in " + guild.name() + ".");
    }

    private VelocityCommandResult executeJoin(IMinecraftVelocityCommandSource source, String[] args) {
        if (args.length < 2) {
            return usage("guild", "join <guild_name>");
        }

        UUID actorPlayerId = requirePlayerId(source);
        rememberKnownPlayer(actorPlayerId, source.platformDisplayName());

        String guildName = joinTail(args, 1);
        GuildMetaData publicGuild = guildStateStore().findPublicByName(guildName)
                .orElseThrow(() -> new IllegalStateException("No public guild named " + guildName + " could be found. Use /guild join <guild_name> and make sure the guild is public."));
        GuildMetaData guild = guildStateStore().requireByGuildId(publicGuild.guildId());

        GuildMetaData joinedGuild = guildMembershipHandler().joinPublicGuild(actorPlayerId, guild.guildId());
        return VelocityCommandResult.completed("Joined public guild " + joinedGuild.name() + " (" + joinedGuild.tag() + ").");
    }

    private VelocityCommandResult executeKick(IMinecraftVelocityCommandSource source, String[] args) {
        if (args.length < 2) {
            return usage("guild", "kick <user|uuid>");
        }

        UUID actorPlayerId = requirePlayerId(source);
        rememberKnownPlayer(actorPlayerId, source.platformDisplayName());

        UUID targetPlayerId = resolveKnownTargetPlayerId(args[1], false)
                .orElseThrow(() -> new IllegalStateException("Could not resolve a player UUID for " + args[1] + ". Use an online player name or a stored UUID."));
        String targetDisplayName = resolveTargetDisplayName(args[1], targetPlayerId);

        GuildMetaData guild = guildMembershipHandler().kickPlayer(actorPlayerId, targetPlayerId);
        return VelocityCommandResult.completed("Removed " + targetDisplayName + " from " + guild.name() + ".");
    }

    private VelocityCommandResult executePromote(IMinecraftVelocityCommandSource source, String[] args) {
        if (args.length < 3) {
            return usage("guild", "promote <user|uuid> <rank>");
        }

        UUID actorPlayerId = requirePlayerId(source);
        rememberKnownPlayer(actorPlayerId, source.platformDisplayName());

        UUID targetPlayerId = resolveKnownTargetPlayerId(args[1], false)
                .orElseThrow(() -> new IllegalStateException("Could not resolve a player UUID for " + args[1] + ". Use an online player name or a stored UUID."));
        GuildRank targetRank = parseGuildRank(args[2]);
        String targetDisplayName = resolveTargetDisplayName(args[1], targetPlayerId);

        GuildMetaData guild = guildMembershipHandler().promoteMember(actorPlayerId, targetPlayerId, targetRank);
        return VelocityCommandResult.completed("Promoted " + targetDisplayName + " to " + targetRank.name() + " in " + guild.name() + ".");
    }

    private VelocityCommandResult executeRole(IMinecraftVelocityCommandSource source, String[] args) {
        if (args.length < 4) {
            return usage("guild", "role assign <user|uuid> <role>");
        }
        if (!"assign".equalsIgnoreCase(args[1])) {
            return VelocityCommandResult.denied("Unknown guild role action: " + args[1] + ". Usage: /guild role assign <user|uuid> <role>.");
        }

        UUID actorPlayerId = requirePlayerId(source);
        rememberKnownPlayer(actorPlayerId, source.platformDisplayName());

        UUID targetPlayerId = resolveKnownTargetPlayerId(args[2], false)
                .orElseThrow(() -> new IllegalStateException("Could not resolve a player UUID for " + args[2] + ". Use an online player name or a stored UUID."));
        GuildRole role = parseGuildRole(args[3]);
        String targetDisplayName = resolveTargetDisplayName(args[2], targetPlayerId);

        GuildMetaData guild = guildRoleHandler().assignRole(actorPlayerId, targetPlayerId, role);
        return VelocityCommandResult.completed("Assigned " + role.name() + " to " + targetDisplayName + " in " + guild.name() + ".");
    }

    private VelocityCommandResult executeBank(IMinecraftVelocityCommandSource source, String[] args) {
        if (args.length < 4) {
            return usage("guild", "bank <deposit|withdraw> <resource> <amount>");
        }

        UUID actorPlayerId = requirePlayerId(source);
        rememberKnownPlayer(actorPlayerId, source.platformDisplayName());

        GuildMetaData guild = guildStateStore().requireByMember(actorPlayerId);
        var actor = guild.members().get(actorPlayerId);
        if (actor == null) {
            throw new IllegalStateException("Guild member state is unavailable.");
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        String resourceToken = args[2];
        long amount = parsePositiveLong(args[3], "amount");
        String reason = args.length >= 5 ? joinTail(args, 4) : "guild bank transaction";

        boolean coinResource = isCoinResource(resourceToken);
        GuildResourceType resourceType = coinResource ? null : parseGuildResourceType(resourceToken);

        return switch (action) {
            case "deposit" -> {
                GuildBankTransaction transaction = buildGuildBankTransaction(guild, actorPlayerId, amount, resourceType, coinResource, reason, true);
                guildBankHandler().recordTransaction(transaction);
                yield VelocityCommandResult.completed("Deposited " + amount + " " + prettyResourceLabel(resourceToken) + " into " + guild.name() + ".");
            }
            case "withdraw" -> {
                if (!actor.hasPermission(GuildPermission.WITHDRAW_GUILD_RESOURCES)) {
                    yield VelocityCommandResult.denied("You need Tier IV access to withdraw guild resources.");
                }
                GuildBankTransaction transaction = buildGuildBankTransaction(guild, actorPlayerId, amount, resourceType, coinResource, reason, false);
                guildBankHandler().recordTransaction(transaction);
                yield VelocityCommandResult.completed("Withdrew " + amount + " " + prettyResourceLabel(resourceToken) + " from " + guild.name() + ".");
            }
            default -> VelocityCommandResult.denied("Unknown guild bank action: " + action + ". Usage: /guild bank <deposit|withdraw> <resource> <amount>.");
        };
    }

    private VelocityCommandResult executeChat(IMinecraftVelocityCommandSource source, String[] args) {
        if (args.length < 3) {
            return usage("guild", "chat <channel> <message>");
        }

        UUID actorPlayerId = requirePlayerId(source);
        rememberKnownPlayer(actorPlayerId, source.platformDisplayName());

        GuildMetaData guild = guildStateStore().requireByMember(actorPlayerId);
        String channel = args[1];
        String content = joinTail(args, 2);
        var sender = guild.members().get(actorPlayerId);
        if (sender == null) {
            throw new IllegalStateException("Guild member state is unavailable.");
        }

        GuildChatMessage message = dependencies().guildChatMessageBuilder()
                .messageId(UUID.randomUUID())
                .guildId(guild.guildId())
                .senderUniversalPlayerId(actorPlayerId)
                .channel(channel)
                .minimumVisibleRank(sender.rank())
                .content(content)
                .createdAt(Instant.now())
                .metadata(Map.of("source", "guild-command", "actor", actorPlayerId.toString()))
                .build();

        guildChatHandler().postMessage(message);
        return VelocityCommandResult.completed("Posted a guild chat message to #" + channel + ".");
    }

    private VelocityCommandResult executeContribute(IMinecraftVelocityCommandSource source, String[] args) {
        if (args.length < 4) {
            return usage("guild", "contribute <sourceType> <points> <coinContribution>");
        }

        UUID actorPlayerId = requirePlayerId(source);
        rememberKnownPlayer(actorPlayerId, source.platformDisplayName());

        GuildMetaData guild = guildStateStore().requireByMember(actorPlayerId);
        String sourceType = args[1];
        long points = parseLong(args[2], "points");
        long coinContribution = parseLong(args[3], "coin contribution");

        GuildContributionEvent event = dependencies().guildContributionEventBuilder()
                .contributionId(UUID.randomUUID())
                .guildId(guild.guildId())
                .universalPlayerId(actorPlayerId)
                .sourceType(sourceType)
                .points(points)
                .coinContribution(coinContribution)
                .resourceContribution(Map.of())
                .createdAt(Instant.now())
                .metadata(Map.of("source", "guild-command", "actor", actorPlayerId.toString()))
                .build();

        guildContributionHandler().recordContribution(event);
        return VelocityCommandResult.completed("Recorded " + points + " contribution points for " + guild.name() + ".");
    }

    private VelocityCommandResult executeLeaderboard(String alias, IMinecraftVelocityCommandSource source, String[] args) {
        if (args.length < 2) {
            return usage(alias, "leaderboard <contribution|bank|members> [limit]");
        }

        String metric = normalizeLeaderboardMetric(args[1]);
        int limit = args.length >= 3 ? parseInt(args[2], "limit") : 5;
        GuildLeaderboardSnapshot snapshot = guildLeaderboardHandler().buildSnapshot(metric, "all_time", limit);

        StringBuilder builder = new StringBuilder();
        builder.append(snapshot.metricKey()).append(" leaderboard");
        if (!snapshot.rows().isEmpty()) {
            builder.append(": ");
            List<GuildLeaderboardRow> rows = snapshot.rows();
            for (int index = 0; index < rows.size(); index++) {
                GuildLeaderboardRow row = rows.get(index);
                if (index > 0) {
                    builder.append(" | ");
                }
                builder.append(index + 1).append(". ").append(row.name()).append("=").append(row.score());
            }
        }
        return VelocityCommandResult.completed(builder.toString());
    }

    private VelocityCommandResult usage(String alias, String usageLine) {
        return VelocityCommandResult.denied("Usage: /" + alias + " " + usageLine);
    }

    private VelocityCommandResult usage(String alias) {
        return VelocityCommandResult.denied("Usage: /" + alias + " <create|invite|join|kick|promote|role|bank|chat|contribute|leaderboard> ...");
    }

    private UUID requirePlayerId(IMinecraftVelocityCommandSource source) {
        try {
            return UUID.fromString(source.platformAccountId());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Guild commands require a player source.");
        }
    }

    private void rememberKnownPlayer(UUID playerId, String displayName) {
        if (displayName != null && !displayName.isBlank()) {
            guildPlayerDirectory().rememberPlayer(playerId, displayName);
        }
    }

    private java.util.Optional<UUID> resolveKnownTargetPlayerId(String targetToken, boolean requireStoredRecord) {
        if (targetToken == null || targetToken.isBlank()) {
            return java.util.Optional.empty();
        }
        resolveOnlineTarget(targetToken).ifPresent(player -> rememberKnownPlayer(player.getUniqueId(), player.getUsername()));
        java.util.Optional<UUID> online = resolveOnlineTarget(targetToken).map(player -> {
            rememberKnownPlayer(player.getUniqueId(), player.getUsername());
            return player.getUniqueId();
        });
        if (online.isPresent()) {
            return online;
        }
        java.util.Optional<UUID> parsed = parseUuid(targetToken);
        if (parsed.isPresent()) {
            if (!requireStoredRecord || guildPlayerDirectory().exists(parsed.get())) {
                return parsed;
            }
            return java.util.Optional.empty();
        }
        java.util.Optional<UUID> knownName = guildPlayerDirectory().findPlayerIdByName(targetToken);
        if (knownName.isPresent() && (!requireStoredRecord || guildPlayerDirectory().exists(knownName.get()))) {
            return knownName;
        }
        return java.util.Optional.empty();
    }

    private java.util.Optional<UUID> parseUuid(String raw) {
        try {
            return java.util.Optional.of(UUID.fromString(raw));
        } catch (IllegalArgumentException ignored) {
            return java.util.Optional.empty();
        }
    }

    private String resolveTargetDisplayName(String targetToken, UUID targetPlayerId) {
        return resolveOnlineTarget(targetToken)
                .map(player -> {
                    rememberKnownPlayer(player.getUniqueId(), player.getUsername());
                    return player.getUsername();
                })
                .or(() -> guildPlayerDirectory().findLastKnownName(targetPlayerId))
                .orElse(targetToken);
    }

    private GuildBankTransaction buildGuildBankTransaction(
            GuildMetaData guild,
            UUID actorPlayerId,
            long amount,
            GuildResourceType resourceType,
            boolean coinResource,
            String reason,
            boolean deposit
    ) {
        long signedAmount = deposit ? amount : -amount;
        Map<GuildResourceType, Integer> resourceDelta = Map.of();
        long coinDelta = 0L;
        if (coinResource) {
            coinDelta = signedAmount;
        } else {
            resourceDelta = Map.of(resourceType, toResourceAmount(signedAmount));
        }
        return dependencies().guildBankTransactionBuilder()
                .transactionId(UUID.randomUUID())
                .guildId(guild.guildId())
                .actorUniversalPlayerId(actorPlayerId)
                .transactionType(deposit ? "guild_bank_deposit" : "guild_bank_withdraw")
                .coinDelta(coinDelta)
                .resourceDelta(resourceDelta)
                .reason(reason)
                .createdAt(Instant.now())
                .metadata(Map.of("source", "guild-command", "actor", actorPlayerId.toString(), "resource", coinResource ? "coins" : resourceType.name()))
                .build();
    }

    private int toResourceAmount(long amount) {
        if (amount < Integer.MIN_VALUE || amount > Integer.MAX_VALUE) {
            throw new IllegalStateException("Resource amount is too large.");
        }
        return Math.toIntExact(amount);
    }

    private GuildResourceType parseGuildResourceType(String raw) {
        String normalized = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        try {
            return GuildResourceType.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Unknown guild resource: " + raw + ". Use coins or one of: " + String.join(", ", java.util.Arrays.stream(GuildResourceType.values()).map(Enum::name).toList()) + ".");
        }
    }

    private boolean isCoinResource(String raw) {
        String normalized = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("coin") || normalized.equals("coins") || normalized.equals("money") || normalized.equals("gold");
    }

    private String prettyResourceLabel(String raw) {
        if (isCoinResource(raw)) {
            return "coins";
        }
        return parseGuildResourceType(raw).name().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    private GuildRank parseGuildRank(String raw) {
        String normalized = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return switch (normalized) {
            case "1", "I", "TIER1", "TIER_I" -> GuildRank.TIER_I;
            case "2", "II", "TIER2", "TIER_II" -> GuildRank.TIER_II;
            case "3", "III", "TIER3", "TIER_III" -> GuildRank.TIER_III;
            case "4", "IV", "TIER4", "TIER_IV" -> GuildRank.TIER_IV;
            case "5", "V", "TIER5", "TIER_V" -> GuildRank.TIER_V;
            default -> throw new IllegalStateException("Unknown guild rank: " + raw + ".");
        };
    }

    private GuildRole parseGuildRole(String raw) {
        String normalized = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return GuildRole.valueOf(normalized);
    }

    private String normalizeLeaderboardMetric(String raw) {
        String normalized = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "contribution", "guild_contribution", "points" -> "guild_contribution";
            case "bank", "bank_value", "guild_bank_value" -> "guild_bank_value";
            case "members", "guild_members" -> "guild_members";
            default -> normalized;
        };
    }

    private long parseLong(String raw, String label) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Invalid " + label + ": " + raw + ".");
        }
    }

    private long parsePositiveLong(String raw, String label) {
        long parsed = parseLong(raw, label);
        if (parsed <= 0L) {
            throw new IllegalStateException(label + " must be positive.");
        }
        return parsed;
    }

    private int parseInt(String raw, String label) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Invalid " + label + ": " + raw + ".");
        }
    }

    private String joinTail(String[] args, int startIndex) {
        StringBuilder builder = new StringBuilder();
        for (int index = startIndex; index < args.length; index++) {
            if (index > startIndex) {
                builder.append(' ');
            }
            builder.append(args[index]);
        }
        return builder.toString();
    }

    private CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestInviteTargets(SuggestionsBuilder builder) {
        return suggestOnlinePlayers(builder);
    }

    private CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestPublicGuilds(SuggestionsBuilder builder) {
        guildStateStore().findAll().stream()
                .filter(GuildMetaData::isPublicGuild)
                .map(GuildMetaData::name)
                .distinct()
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    private CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestBankResources(SuggestionsBuilder builder) {
        builder.suggest("coins");
        for (GuildResourceType resourceType : GuildResourceType.values()) {
            builder.suggest(resourceType.name().toLowerCase(Locale.ROOT));
        }
        return builder.buildFuture();
    }

    private String stringArg(CommandContext<CommandSource> context, String name) {
        return StringArgumentType.getString(context, name);
    }

    private long longArg(CommandContext<CommandSource> context, String name) {
        return LongArgumentType.getLong(context, name);
    }

    private int intArg(CommandContext<CommandSource> context, String name) {
        return IntegerArgumentType.getInteger(context, name);
    }
}


