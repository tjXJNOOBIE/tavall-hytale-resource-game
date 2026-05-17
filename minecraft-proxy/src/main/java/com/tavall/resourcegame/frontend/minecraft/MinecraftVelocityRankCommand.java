package com.tavall.resourcegame.frontend.minecraft;

import com.tavall.resourcegame.shared.frontend.RankRequest;
import com.tavall.resourcegame.shared.frontend.RankResponse;
import com.tavall.resourcegame.shared.permissions.UniversalPermissionRole;
import com.tavall.resourcegame.shared.permissions.UniversalPermissionSubject;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class MinecraftVelocityRankCommand implements SimpleCommand, IMinecraftFrontendDomain, IDependencyInjectableConcrete {
    @Override
    public void execute(Invocation invocation) {
        MinecraftVelocityCommandResult result = execute(commandSource(invocation), invocation.alias(), invocation.arguments());
        invocation.source().sendMessage(Component.text(result.message()));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }

    MinecraftVelocityCommandResult execute(MinecraftVelocityCommandSource source, String alias, String[] arguments) {
        try {
            RankRequest request = buildRequest(source, arguments);
            RankResponse response = getMinecraftControlCommandClient().submitRankRequest(request);
            return new MinecraftVelocityCommandResult(response.success(), formatResponse(response));
        } catch (IllegalArgumentException exception) {
            return MinecraftVelocityCommandResult.denied(exception.getMessage());
        } catch (RuntimeException exception) {
            return MinecraftVelocityCommandResult.denied("Rank command failed: " + safeMessage(exception));
        }
    }

    private RankRequest buildRequest(MinecraftVelocityCommandSource source, String[] arguments) {
        String requestId = "minecraft-rank-" + UUID.randomUUID();
        Map<String, String> context = new LinkedHashMap<>(source.metadata());
        context.put("sourceType", source.sourceType());
        context.put("proxy", getMinecraftProxyConfig().serverId());
        context.put("alias", "rank");
        context.put("surfaceIdentity", "VELOCITY_PROXY");

        if (arguments.length == 0 || isListOperation(arguments)) {
            return RankRequest.list(
                    requestId,
                    com.tavall.resourcegame.shared.frontend.ResourceGameFrontendPlatform.MINECRAFT,
                    source.platformAccountId(),
                    source.platformDisplayName(),
                    context,
                    Instant.now().toEpochMilli()
            );
        }

        if (isSetOperation(arguments) || isShorthandSetOperation(arguments) || isRemoveOperation(arguments)) {
            String target;
            String roleValue;
            if (isSetOperation(arguments)) {
                if (arguments.length < 3) {
                    throw new IllegalArgumentException("Usage: /rank [list|inspect <target>|set <target> <role>|remove <target> [fallback_role]]");
                }
                target = arguments[1];
                roleValue = arguments[2];
            } else if (isRemoveOperation(arguments)) {
                if (arguments.length < 2) {
                    throw new IllegalArgumentException("Usage: /rank [list|inspect <target>|set <target> <role>|remove <target> [fallback_role]]");
                }
                target = arguments[1];
                roleValue = arguments.length > 2 ? arguments[2] : "MEMBER";
            } else {
                target = arguments[0];
                roleValue = arguments[1];
            }
            return RankRequest.setRole(
                    requestId,
                    com.tavall.resourcegame.shared.frontend.ResourceGameFrontendPlatform.MINECRAFT,
                    source.platformAccountId(),
                    source.platformDisplayName(),
                    target,
                    target,
                    parseRole(roleValue),
                    context,
                    Instant.now().toEpochMilli()
            );
        }

        if (isInspectOperation(arguments)) {
            String target = arguments.length > 1 ? arguments[1] : source.platformAccountId();
            String targetDisplayName = arguments.length > 1 ? arguments[1] : source.platformDisplayName();
            return RankRequest.inspect(
                    requestId,
                    com.tavall.resourcegame.shared.frontend.ResourceGameFrontendPlatform.MINECRAFT,
                    source.platformAccountId(),
                    source.platformDisplayName(),
                    target,
                    targetDisplayName,
                    context,
                    Instant.now().toEpochMilli()
            );
        }

        if (arguments.length == 1) {
            return RankRequest.inspect(
                    requestId,
                    com.tavall.resourcegame.shared.frontend.ResourceGameFrontendPlatform.MINECRAFT,
                    source.platformAccountId(),
                    source.platformDisplayName(),
                    arguments[0],
                    arguments[0],
                    context,
                    Instant.now().toEpochMilli()
            );
        }

        if (arguments.length == 2) {
            return RankRequest.setRole(
                    requestId,
                    com.tavall.resourcegame.shared.frontend.ResourceGameFrontendPlatform.MINECRAFT,
                    source.platformAccountId(),
                    source.platformDisplayName(),
                    arguments[0],
                    arguments[0],
                    parseRole(arguments[1]),
                    context,
                    Instant.now().toEpochMilli()
            );
        }

        throw new IllegalArgumentException("Usage: /rank [list|inspect <target>|set <target> <role>|remove <target> [fallback_role]]");
    }

    private String formatResponse(RankResponse response) {
        if (!response.success()) {
            return response.message();
        }
        if (response.subject() != null) {
            return response.message() + " " + formatSubject(response.subject());
        }
        if (!response.subjects().isEmpty()) {
            String subjects = response.subjects().stream()
                    .map(this::formatSubject)
                    .collect(Collectors.joining(" | "));
            return response.message() + " " + subjects;
        }
        return response.message();
    }

    private String formatSubject(UniversalPermissionSubject subject) {
        String permissions = subject.role().permissions().stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.joining(", "));
            return subject.displayName() + "=" + subject.role().name() + " [" + permissions + "]";
        }

    private UniversalPermissionRole parseRole(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Rank role is required.");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "member", "default", "user" -> UniversalPermissionRole.MEMBER;
            case "mod", "moderator" -> UniversalPermissionRole.MODERATOR;
            case "admin" -> UniversalPermissionRole.ADMIN;
            case "owner" -> UniversalPermissionRole.OWNER;
            case "system" -> UniversalPermissionRole.SYSTEM;
            default -> {
                try {
                    yield UniversalPermissionRole.valueOf(value.trim().toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException("Unknown rank role " + value + ". Use MEMBER, MODERATOR, ADMIN, OWNER, or SYSTEM.");
                }
            }
        };
    }

    private boolean isInspectOperation(String[] arguments) {
        return arguments.length > 0 && isOneOf(arguments[0], "inspect", "get", "show", "view");
    }

    private boolean isSetOperation(String[] arguments) {
        return arguments.length > 0 && isOneOf(arguments[0], "set", "update", "grant");
    }

    private boolean isShorthandSetOperation(String[] arguments) {
        return arguments.length == 2 && !isInspectOperation(arguments) && !isListOperation(arguments) && !isRemoveOperation(arguments);
    }

    private boolean isRemoveOperation(String[] arguments) {
        return arguments.length > 0 && isOneOf(arguments[0], "remove", "revoke");
    }

    private boolean isListOperation(String[] arguments) {
        return arguments.length > 0 && isOneOf(arguments[0], "list", "all");
    }

    private boolean isOneOf(String value, String... options) {
        String normalized = normalize(value);
        return Arrays.stream(options).anyMatch(option -> normalize(option).equals(normalized));
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message;
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
